package com.x.workflow.condition;

import com.x.workflow.condition.impl.QLCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class QLConditionTest {
    private static final Logger LOGGER = LogManager.getLogger(QLConditionTest.class);

    @Test
    public void testMatch() {
        String rulePaas = "a + b > c";
        QLCondition qlCondition1 = new QLCondition();
        Map<String, Object> parametersPaas = new HashMap<>();
        parametersPaas.put("a", 1);
        parametersPaas.put("b", 2);
        parametersPaas.put("c", 2);
        boolean resultPaas = qlCondition1.match(rulePaas, parametersPaas);
        LOGGER.info("rule: {} result: {}", rulePaas, resultPaas);
        Assert.assertTrue(resultPaas);

        String ruleFail = "(a + b)/100 > c";
        QLCondition qlCondition2 = new QLCondition();
        Map<String, Object> parametersFail = new HashMap<>();
        parametersFail.put("a", 1);
        parametersFail.put("b", 2);
        parametersFail.put("c", 2);
        boolean resultFail = qlCondition2.match(ruleFail, parametersFail);
        LOGGER.info("rule: {} result: {}", ruleFail, resultFail);
        Assert.assertFalse(resultFail);

        String ruleException = "(a + b)/100 > c";
        QLCondition qlCondition3 = new QLCondition();
        Map<String, Object> parametersException = new HashMap<>();
        parametersFail.put("a", 1);
        parametersFail.put("b", 2);
        boolean resultException = qlCondition3.match(ruleException, parametersException);
        LOGGER.info("rule: {} result: {}", ruleException, resultException);
        Assert.assertFalse(resultException);
    }
}
