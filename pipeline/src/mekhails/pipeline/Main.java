package mekhails.pipeline;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main
{
    private final static String LOGGER_FILE = "log_pipeline.log";
    private final static String LOGGER_NAME = "pipeline";

    public static void main(String[] Args) throws IOException {

        Logger logger = Logger.getLogger(LOGGER_NAME);

        FileHandler fh = new FileHandler(LOGGER_FILE);
        fh.setFormatter(new SimpleFormatter());

        logger.addHandler(fh);
        logger.setUseParentHandlers(false);

        if (Args == null || Args.length == 0)
        {
            logger.log(Level.SEVERE, Log.ERROR.COMMAND_PROMPT.name);
            return;
        }
        String configFilename = Args[0];

        Manager mng = new Manager(configFilename, logger);

        mng.configureAndConstructPipeline();

        if (mng.isEverythingAvailable())
            mng.run();
    }
}
