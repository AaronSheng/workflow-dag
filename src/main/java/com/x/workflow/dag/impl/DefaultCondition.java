package com.x.workflow.dag.impl;

import com.x.workflow.dag.Condition;

public class DefaultCondition implements Condition {
    private String conditionName;
    private String conditionRule;

    @Override
    public String getConditionName() {
        return conditionName;
    }

    @Override
    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    @Override
    public String getConditionRule() {
        return conditionRule;
    }

    @Override
    public void setConditionRule(String conditionRule) {
        this.conditionRule = conditionRule;
    }
}
