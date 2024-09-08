package com.x.workflow.condition.impl;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.x.workflow.condition.Condition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QLCondition implements Condition {
    private static final Logger LOGGER = LogManager.getLogger(QLCondition.class);
    private static final String CONDITION_NAME = "QLCondition";

    @Override
    public String getConditionName() {
        return CONDITION_NAME;
    }

    @Override
    public boolean match(String rule, Map<String, Object> contextParameters) {
        ExpressRunner runner = new ExpressRunner();
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.putAll(contextParameters);
        List<String> errorList = new ArrayList<>();

        try {
            return (Boolean) runner.execute(rule, context, errorList, true, false);
        } catch (Exception e) {
            LOGGER.error("rule [{}] match fail, errors: {}", rule, errorList, e);
        }
        return false;
    }
}
