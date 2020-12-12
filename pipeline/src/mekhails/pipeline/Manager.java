package mekhails.pipeline;

import javafx.util.Pair;
import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Manager extends AConfigurable {

    /**
     * Vocabulary of Manager
     */
    private enum LexemeManager implements ILexeme
    {
        READER("reader", SemanticAnalyzer.Semantic.READER),
        INPUT_FILE("input file", SemanticAnalyzer.Semantic.FILE_IN),

        EXECUTOR("worker", SemanticAnalyzer.Semantic.EXECUTOR),

        WRITER("writer", SemanticAnalyzer.Semantic.WRITER),
        OUTPUT_FILE("output file", SemanticAnalyzer.Semantic.FILE_OUT);

        LexemeManager(String nameInConfig_, SemanticAnalyzer.Semantic semantic_)
            {nameInConfig = nameInConfig_; semantic = semantic_;}

        @Override
        public SemanticAnalyzer.Semantic getSemantic() { return semantic; }
        public String getNameInConfig() { return nameInConfig; }

        private final String nameInConfig;
        private final SemanticAnalyzer.Semantic semantic;
    }

    Manager(String configFilename_, Logger logger_)
    {
        configFilename = configFilename_; logger = logger_;
    }

    @Override
    protected LexemeAndRule[] setOfRulesForVocabulary()
    {
        LexemeAndRule[] setRules =
                {
                        new LexemeAndRule(LexemeManager.READER, paramVal -> {
                            Pair<IReader, String> readerAndConfig = (Pair<IReader, String>)paramVal;
                            reader = readerAndConfig.getKey();
                            return reader.setConfig(readerAndConfig.getValue());
                        }),

                        new LexemeAndRule(LexemeManager.INPUT_FILE, obj -> {
                            inFilename = (String)obj;
                            return RC.CODE_SUCCESS;
                        }),

                        new LexemeAndRule(LexemeManager.EXECUTOR, paramVal -> {
                            Pair<IExecutor, String>[] workersAndFilesArr = (Pair<IExecutor, String>[])paramVal;

                            RC code = RC.CODE_SUCCESS;
                            executors = new IExecutor[workersAndFilesArr.length];
                            for (int i = 0; i<workersAndFilesArr.length; i++)
                            {
                                Pair<IExecutor, String> workerAndFile = workersAndFilesArr[i];
                                executors[i] = workerAndFile.getKey();
                                code = executors[i].setConfig(workerAndFile.getValue());
                                if (code != RC.CODE_SUCCESS)
                                    return code;
                            }
                            return RC.CODE_SUCCESS;
                        }),

                        new LexemeAndRule(LexemeManager.WRITER, paramVal -> {
                            Pair<IWriter, String> writerAndConfig = (Pair<IWriter, String>)paramVal;
                            writer = writerAndConfig.getKey();
                            return writer.setConfig(writerAndConfig.getValue());
                        }),

                        new LexemeAndRule(LexemeManager.OUTPUT_FILE, paramVal -> {
                            outFilename = (String)paramVal;
                            return RC.CODE_SUCCESS;
                        })
                };

        return setRules;
    }

    RC configureAndConstructPipeline()
    {
        try
        {
            FileInputStream cfgStream = new FileInputStream(configFilename);

            code = configure(cfgStream, logger);

            if (code != RC.CODE_SUCCESS)
            {
                Log.logError(logger, code);
                return code;
            }

            if (!isEverythingAvailable())
                return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;

            code = linkEverything();
        }
        catch (FileNotFoundException e) {
            code = RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
            Log.logError(logger, code);
        }
        finally {
            return code;
        }
    }

    RC run()
    {
        try
        {
            if (!isEverythingAvailable())
                return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;

            FileInputStream fis = new FileInputStream(inFilename);
            FileOutputStream fos = new FileOutputStream(outFilename);

            reader.setInputStream(fis);
            writer.setOutputStream(fos);

            RC code = reader.execute();

            fis.close();
            fos.close();

            return code;
        }
        catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, Log.ERROR.CONFIG.name);
            return RC.CODE_INVALID_INPUT_STREAM;
        } catch (IOException e) {
            logger.log(Level.SEVERE, Log.ERROR.WRITER.name);
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    boolean isEverythingAvailable()
    {
        return  ((reader != null) && (writer != null) && (executors != null) && code == RC.CODE_SUCCESS);
    }

    private RC linkEverything()
    {
        RC code = RC.CODE_SUCCESS;
        for (int i = 0; i < executors.length; i++)
        {
            IExecutor executorCur = executors[i];
            if (i > 0)
            {
                executorCur.setProducer(executors[i-1]);
                if (code != RC.CODE_SUCCESS)
                    return code;
            }
            if (i < executors.length - 1)
            {
                code = executorCur.setConsumer(executors[i+1]);
                if (code != RC.CODE_SUCCESS)
                    return code;
            }
        }
        if (executors.length == 0)
        {
            reader.setConsumer(writer);
            writer.setProducer(reader);
        }
        else
        {
            reader.setConsumer(executors[0]);
            executors[0].setProducer(reader);

            writer.setProducer(executors[executors.length-1]);
            executors[executors.length-1].setConsumer(writer);
        }
        return RC.CODE_SUCCESS;
    }

    private RC code = RC.CODE_SUCCESS;

    private IReader reader;
    private IWriter writer;
    private IExecutor[] executors;

    private String inFilename;
    private String outFilename;
    private String configFilename;

    private final Logger logger;
}
