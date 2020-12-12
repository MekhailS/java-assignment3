package mekhails.writer;

import ru.spbstu.pipeline.*;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ByteWriter extends AConfigurable implements IWriter {

    /**
     * Vocabulary of Writer
     */
    private enum LexemeWriter implements ILexeme
    {
        BUFFER_SIZE("buffer size", SemanticAnalyzer.Semantic.SIZE);

        LexemeWriter(String nameInConfig_, SemanticAnalyzer.Semantic semantic_)
            {nameInConfig = nameInConfig_; semantic = semantic_;}

        @Override
        public SemanticAnalyzer.Semantic getSemantic() { return semantic; }
        public String getNameInConfig() { return nameInConfig; }

        private final String nameInConfig;
        private final SemanticAnalyzer.Semantic semantic;
    }

    @Override
    protected LexemeAndRule[] setOfRulesForVocabulary()
    {
        LexemeAndRule[] setRules =
                {
                        new LexemeAndRule(LexemeWriter.BUFFER_SIZE, paramVal -> {
                            bufferSize = (Integer)paramVal;
                            return RC.CODE_SUCCESS;
                        })
                };

        return setRules;
    }


    public ByteWriter(Logger logger_)
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
        return write(data);
    }

    @Override
    public RC setConsumer(IConsumer consumer)
    {
        return null;
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
    public RC setOutputStream(FileOutputStream fileOutputStream)
    {
        bos = new BufferedOutputStream(fileOutputStream);
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

    private RC write(byte[] buffer)
    {
        try
        {
            if (buffer == null)
                return RC.CODE_SUCCESS;

            int i = 0;
            while (i*bufferSize + bufferSize <= buffer.length - 1)
            {
                bos.write(buffer, i*bufferSize, bufferSize);
                i += 1;
            }
            bos.write(buffer,i*bufferSize, buffer.length - i*bufferSize);
            bos.flush();
            return RC.CODE_SUCCESS;
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, Log.ERROR.WRITER.name);
            return RC.CODE_FAILED_TO_WRITE;
        }
    }

    private static final TYPE[] POTENTIAL_TYPES = {TYPE.BYTE, TYPE.SHORT};

    private byte[] data;

    private IMediator mediatorProducer;

    private TYPE typeIn = TYPE.BYTE;

    private int bufferSize;
    private BufferedOutputStream bos;

    private final Logger logger;
}
