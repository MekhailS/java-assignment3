package mekhails.executor;

import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reverser extends AConfigurable implements IExecutor {

    /**
     * Vocabulary of Reverser
     */
    private enum LexemeReverser implements ILexeme
    {
        REVERSING("enable reversing", SemanticAnalyzer.Semantic.BOOL);

        LexemeReverser(String nameInConfig_, SemanticAnalyzer.Semantic semantic_)
            {nameInConfig = nameInConfig_; semantic = semantic_;}

        public SemanticAnalyzer.Semantic getSemantic() { return semantic; }
        public String getNameInConfig() { return nameInConfig; }

        public final String nameInConfig;
        public final SemanticAnalyzer.Semantic semantic;
    }

    @Override
    protected LexemeAndRule[] setOfRulesForVocabulary()
    {
        LexemeAndRule[] setRules =
                {
                        new LexemeAndRule(LexemeReverser.REVERSING, paramVal -> {
                            enableReversing = (Boolean)paramVal;
                            return RC.CODE_SUCCESS;
                        })
                };

        return setRules;
    }

    private class ReverserMediator implements IMediator
    {
        ReverserMediator(TYPE type_)
            { type = type_; }

        @Override
        public Object getData()
        {
            if (data == null)
                return null;

            Object resData = null;
            switch (type)
            {
                case BYTE:
                        resData = Arrays.copyOf(data, data.length);
                    break;
                case SHORT:
                        resData = ArrayConverter.byteToShort(data);
                    break;
            }
            return resData;
        }

        private final TYPE type;
    }

    public Reverser(Logger logger_)
    {logger = logger_;}

    @Override
    public RC execute()
    {
        Object dataFromProducer = mediatorProducer.getData();
        switch (typeIn)
        {
            case BYTE:
                data = (byte[]) dataFromProducer;
                break;
            case SHORT:
                data = ArrayConverter.shortToByte((short[])dataFromProducer);
                break;
        }
        if (enableReversing)
            reverseBitsInBuffer(data);

        return consumer.execute();
    }

    @Override
    public TYPE[] getOutputTypes()
    {
        return Arrays.copyOf(POTENTIAL_TYPES, POTENTIAL_TYPES.length);
    }

    @Override
    public IMediator getMediator(TYPE type)
    {
        return new ReverserMediator(type);
    }

    @Override
    public RC setProducer(IProducer producer)
    {
        TYPE[] typesProducer = producer.getOutputTypes();
        for (TYPE typeMy : POTENTIAL_TYPES)
        {
            for (TYPE typePr: typesProducer)
            {
                if (typeMy == typePr)
                {
                    typeIn = typeMy;
                    mediatorProducer = producer.getMediator(typeIn);
                    return RC.CODE_SUCCESS;
                }
            }
        }
        return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
    }

    @Override
    public RC setConsumer(IConsumer o)
    {
        consumer = o;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String s)
    {
        try
        {
            FileInputStream cfgStream = new FileInputStream(s);

            return configure(cfgStream, logger);
        }
        catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, Log.ERROR.CONFIG.name);
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    private void reverseBitsInBuffer(byte[] buffer)
    {
        if (buffer == null)
            return;
        reverseBytesInBuffer(buffer);
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = reverseBitsInByte(buffer[i]);
        }
    }

    private void reverseBytesInBuffer(byte[] buffer)
    {
        for (int i = 0; i < buffer.length/2; i++)
        {
            byte tmp = buffer[i];
            buffer[i] = buffer[buffer.length - 1 - i];
            buffer[buffer.length - 1 - i] = tmp;
        }
    }

    private byte reverseBitsInByte(byte x)
    {
        byte res = 0;
        for (int i = 0; i < 8; i++)
        {
            res <<= 1;
            res |= ( (x >> i) & 1);
        }
        return res;
    }

    private static final TYPE[] POTENTIAL_TYPES = {TYPE.SHORT, TYPE.BYTE};

    private byte[] data;

    private IConsumer consumer;
    private IMediator mediatorProducer;

    private TYPE typeIn = TYPE.BYTE;

    private boolean enableReversing;

    private final Logger logger;
}
