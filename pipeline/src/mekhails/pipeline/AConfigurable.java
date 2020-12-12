package mekhails.pipeline;

import ru.spbstu.pipeline.RC;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * Abstract configurable class, examples of such class:
 * Manager, Reverser, Reader, Writer
 */
abstract class AConfigurable
{
    /**
     * These rules will be applied to semantically validated parameters
     * all rules must be defined in inherited class for each lexeme in vocabulary of this class
     */
    protected abstract LexemeAndRule[] setOfRulesForVocabulary();

    /**
     * Configure instance by stream of config file
     * @param cfgStream stream of config file
     * @return RC
     */
    protected RC configure(FileInputStream cfgStream, Logger logger)
    {
        LexemeAndRule[] vocabularyAndRules = setOfRulesForVocabulary();

        // Create array of lexemes only (vocabulary)
        ILexeme[] vocabulary = new ILexeme[vocabularyAndRules.length];
        for (int i = 0; i < vocabularyAndRules.length; i++)
        {
            vocabulary[i] = vocabularyAndRules[i].getLexeme();
        }

        // Create configReader (all config tokens are parsed) with vocabulary
        ConfigReader configReader = ConfigReader.getConfigByVocabulary(vocabulary, cfgStream, logger);

        if (configReader == null)
            return RC.CODE_CONFIG_GRAMMAR_ERROR;

        return validateSemanticAndApplyRules(configReader, vocabularyAndRules, logger);
    }

    /**
     * Do semantic validation and apply rules to all semantically validated parameters
     * @param configReader ConfigReader with all config tokens parsed
     * @param vocabularyAndRules array of 'pairs' lexeme-rule
     * @return RC
     */
    private static RC validateSemanticAndApplyRules(ConfigReader configReader, LexemeAndRule[] vocabularyAndRules, Logger logger)
    {
        for (LexemeAndRule lexemeAndRule : vocabularyAndRules)
        {
            ILexeme lexeme = lexemeAndRule.getLexeme();
            Rule rule = lexemeAndRule.getRule();

            // get value of parameter as string from configReader
            ArrayList<String> paramValAsString = configReader.getParameter(lexeme.getNameInConfig());

            // Do semantic validation and get semantically parsed paramValue
            Object paramValue = SemanticAnalyzer.parseParam(paramValAsString, lexeme.getSemantic(), logger);

            if (paramValue == null && lexeme.getSemantic() != SemanticAnalyzer.Semantic.EMPTY)
                return RC.CODE_CONFIG_SEMANTIC_ERROR;

            RC code = rule.apply(paramValue);

            if (code != RC.CODE_SUCCESS)
                return code;
        }
        return RC.CODE_SUCCESS;
    }
}
