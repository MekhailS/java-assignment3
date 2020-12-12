package mekhails.reader;

import javafx.util.Pair;
import ru.spbstu.pipeline.IExecutor;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.RC;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class SemanticAnalyzer {

    final static String propertiesDelimiter = ";";

    enum Semantic
    {
        EMPTY,
        FILE_IN,
        FILE_OUT,
        BOOL,
        SIZE,
        READER,
        WRITER,
        EXECUTOR
    }

    static Object parseParam(ArrayList<String> paramAsString, Semantic parameterSemantic, Logger logger)
    {
        try
        {
            if (paramAsString == null)
                return null;

            Object res = null;

            switch(parameterSemantic)
            {
                case EMPTY:
                    break;

                case FILE_OUT:
                {
                    String outFilename = paramAsString.get(0);
                    File outFile = new File(outFilename);
                    if (!outFile.canWrite() && !outFile.createNewFile())
                    {
                        Log.logError(logger, RC.CODE_FAILED_TO_WRITE);
                        break;
                    }
                    res = outFilename;
                    break;
                }

                case FILE_IN:
                {
                    String inFilename = paramAsString.get(0);
                    File inFile = new File(inFilename);
                    if (!inFile.canRead())
                    {
                        Log.logError(logger, RC.CODE_FAILED_TO_READ);
                        break;
                    }

                    res = inFilename;
                    break;
                }

                case SIZE:
                {
                    Integer size = Integer.parseInt(paramAsString.get(0));
                    if (size < 0 || size % 2 == 1)
                        size = null;

                    res = size;
                    break;
                }

                case BOOL:
                {
                    String word = paramAsString.get(0);
                    if (word.equals("True"))
                        res = true;
                    else if (word.equals("False"))
                        res = false;
                    break;
                }

                case WRITER :
                {
                    String[] tokens = paramAsString.get(0).split(propertiesDelimiter);

                    String cfgFilename = tokens[1].trim();
                    File cfgFile = new File(cfgFilename);
                    if (!cfgFile.canWrite())
                    {
                        Log.logError(logger, RC.CODE_FAILED_TO_READ);
                        break;
                    }

                    String className = tokens[0].trim();
                    Class<?> writerClass = Class.forName(className);
                    Constructor<?> writerConstructor = writerClass.getConstructor(Logger.class);
                    IWriter writer = (IWriter) writerConstructor.newInstance(logger);

                    res = new Pair<IWriter, String>(writer, cfgFilename);
                    break;
                }

                case READER:
                {
                    String[] tokens = paramAsString.get(0).split(propertiesDelimiter);

                    String cfgFilename = tokens[1].trim();
                    File cfgFile = new File(cfgFilename);
                    if (!cfgFile.canRead())
                    {
                        Log.logError(logger, RC.CODE_FAILED_TO_READ);
                        break;
                    }

                    String className = tokens[0].trim();
                    Class<?> readerClass = Class.forName(className);
                    Constructor<?> readerConstructor = readerClass.getConstructor(Logger.class);
                    IReader reader = (IReader) readerConstructor.newInstance(logger);

                    res = new Pair<IReader, String>(reader, cfgFilename);
                    break;
                }

                case EXECUTOR:
                {
                    Pair<IExecutor, String>[] resArr = new Pair[paramAsString.size()];
                    for (int i = 0; i<paramAsString.size(); i++)
                    {
                        String[] tokens = paramAsString.get(i).split(propertiesDelimiter);

                        String cfgFilename = tokens[1].trim();

                        String className = tokens[0].trim();
                        Class<?> executorClass = Class.forName(className);
                        Constructor<?> executorConstructor = executorClass.getConstructor(Logger.class);
                        IExecutor executor = (IExecutor) executorConstructor.newInstance(logger);

                        File cfgFile = new File(cfgFilename);

                        if (!cfgFile.canRead())
                        {
                            Log.logError(logger, RC.CODE_FAILED_TO_READ);
                            resArr = null;
                            break;
                        }

                        resArr[i] = new Pair<IExecutor, String>(executor, cfgFilename);
                    }
                    res = resArr;
                    break;
                }
            }
            return res;
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | IOException e) {
            logger.log(Level.SEVERE, Log.ERROR.CONFIG.name);
        }
        return null;
    }
}
