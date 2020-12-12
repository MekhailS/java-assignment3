package mekhails.reader;

import ru.spbstu.pipeline.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ByteReader extends AConfigurable implements IReader {

    /**
     * Vocabulary of Reader
     */
    private enum LexemeReader implements ILexeme
    {
        BUFFER_SIZE("buffer size", SemanticAnalyzer.Semantic.SIZE);

        LexemeReader(String nameInConfig_, SemanticAnalyzer.Semantic semantic_)
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
                        new LexemeAndRule(LexemeReader.BUFFER_SIZE, paramVal -> {
                            bufferSize = (Integer)paramVal;
                            return RC.CODE_SUCCESS;
                        })
                };

        return setRules;
    }


    public ByteReader(Logger logger_)
        {logger = logger_;}


    private class ByteReaderMediator implements IMediator
    {
        ByteReaderMediator(TYPE type_)
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

    @Override
    public RC setConsumer(IConsumer consumer_)
    {
        consumer = consumer_;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer iProducer)
    {
        return RC.CODE_SUCCESS;
    }

    @Override
    public TYPE[] getOutputTypes()
    {
        return POTENTIAL_TYPES;
    }

    @Override
    public IMediator getMediator(TYPE type)
    {
        return new ByteReaderMediator(type);
    }

    @Override
    public RC setInputStream(FileInputStream fileInputStream)
    {
        bis = new BufferedInputStream(fileInputStream);
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute()
    {
        try
        {
            data = new byte[bufferSize];
            int bytesRead = 0;
            boolean isEnd = false;
            while (true)
            {
                if (isEnd)
                {
                    data = null;
                    return consumer.execute();
                }

                bytesRead = bis.read(data, 0, bufferSize);
                if (bytesRead == -1)
                {
                    data = null;
                    return consumer.execute();
                }

                if (bytesRead != data.length)
                {
                    if (bytesRead % 2 == 1)
                    {
                        logger.log(Level.SEVERE, Log.ERROR.ODD_IN_FILE.name);
                        Log.logError(logger, RC.CODE_INVALID_INPUT_STREAM);
                        return RC.CODE_INVALID_INPUT_STREAM;
                    }
                    data = Arrays.copyOf(data, bytesRead);
                    isEnd = true;
                }

                RC code = consumer.execute();
                if (code != RC.CODE_SUCCESS)
                    return code;
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, Log.ERROR.READER.name);
            return RC.CODE_FAILED_TO_READ;
        }
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

    private static final TYPE[] POTENTIAL_TYPES = {TYPE.BYTE, TYPE.SHORT};

    private byte[] data;

    private IConsumer consumer;

    private int bufferSize;
    private BufferedInputStream bis;

    private final Logger logger;
}
