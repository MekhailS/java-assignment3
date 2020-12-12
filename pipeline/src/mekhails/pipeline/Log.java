package mekhails.pipeline;

import ru.spbstu.pipeline.RC;

import java.util.logging.Level;
import java.util.logging.Logger;

class Log {

    enum ERROR {
        ODD_IN_FILE("number of bytes in  input file is odd"),
        OK("no error"),
        GRAMMAR_ERROR("grammar error occured"),
        SEMANTIC_ERROR("semantic error occured"),
        COMMAND_PROMPT("error with command prompt arguments"),
        CONFIG("error occurred with config file"),
        READER("error while reading/opening input file"),
        WRITER("error while writing/opening output file");

        ERROR(String name_) { name = name_;}

        public final String name;
    }

    static void logError(Logger logger, RC error)
    {
        switch(error)
        {
            case CODE_SUCCESS:
                logger.log(Level.SEVERE, ERROR.OK.name);
                break;
            case CODE_INVALID_ARGUMENT:
                logger.log(Level.SEVERE, ERROR.COMMAND_PROMPT.name);
                break;
            case CODE_FAILED_TO_READ:
            case CODE_INVALID_INPUT_STREAM:
                logger.log(Level.SEVERE, ERROR.READER.name);
                break;
            case CODE_FAILED_TO_WRITE:
            case CODE_INVALID_OUTPUT_STREAM:
                logger.log(Level.SEVERE, ERROR.WRITER.name);
                break;
            case CODE_CONFIG_GRAMMAR_ERROR:
                logger.log(Level.SEVERE, ERROR.GRAMMAR_ERROR.name);
                break;
            case CODE_CONFIG_SEMANTIC_ERROR:
                logger.log(Level.SEVERE, ERROR.SEMANTIC_ERROR.name);
                break;
            case CODE_FAILED_PIPELINE_CONSTRUCTION:
                logger.log(Level.SEVERE, ERROR.CONFIG.name);
                break;
        }
    }
}
