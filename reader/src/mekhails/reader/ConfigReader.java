package mekhails.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConfigReader
{
    final static String DELIMITER = ":";
    final static String PREFIX_COMMENT = "#";

    static ConfigReader getConfigByVocabulary(ILexemeConfig[] params, FileInputStream cfgStream, Logger logger)
    {
        String[] paramsNamesInConfig = new String[params.length];
        for (int i = 0; i<params.length; i++)
            paramsNamesInConfig[i] = params[i].getNameInConfig();

        ConfigReader configReader = new ConfigReader(paramsNamesInConfig, logger);
        if (!configReader.readConfig(cfgStream))
            return null;

        if (!configReader.isConfigValid())
            return null;

        return configReader;
    }

    boolean isConfigValid()
    {
        return (params != null && allowedConfigParamsNames != null && params.size() == allowedConfigParamsNames.length);
    }

    ArrayList<String> getParameter(String parameterName) { return params.get(parameterName); }

    private ConfigReader(String[] allowedConfigParamsNames_, Logger logger_)
    {
        allowedConfigParamsNames = allowedConfigParamsNames_;
        logger = logger_;
    }

    private boolean readConfig(FileInputStream cfgStream)
    {
        try
        {
            if (allowedConfigParamsNames == null)
                return false;

            BufferedReader bufR = new BufferedReader(new InputStreamReader(cfgStream));

            for (String line = bufR.readLine(); line != null; line = bufR.readLine())
            {
                if (line.equals("") || line.trim().startsWith(PREFIX_COMMENT))
                    continue;
                String[] tokens = line.split(DELIMITER);

                if (tokens.length < 2) {
                    logger.log(Level.SEVERE, Log.ERROR.CONFIG.name);
                    return false;
                }

                for (int i = 0; i < tokens.length; i++)
                {
                    String curToken = tokens[i].trim();

                    if (Arrays.asList(allowedConfigParamsNames).contains(curToken))
                    {
                        if (params.containsKey(curToken))
                        {
                            ArrayList<String> parameterNames = params.get(curToken);
                            parameterNames.add(tokens[++i].trim());
                        }
                        else
                        {
                            ArrayList<String> parameterNames = new ArrayList<>();
                            parameterNames.add(tokens[++i].trim());

                            params.put(curToken, parameterNames);
                        }
                    }
                }
            }
            if (params.size() != allowedConfigParamsNames.length)
                return false;
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, Log.ERROR.CONFIG.name);
        }
        return false;
    }

    private HashMap<String, ArrayList<String>> params = new HashMap<>();
    private String[] allowedConfigParamsNames;

    private final Logger logger;
}
