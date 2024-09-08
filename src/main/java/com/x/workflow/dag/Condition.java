package com.x.workflow.dag;

public interface Condition {
    void setConditionName(String conditionName);

    String getConditionName();

    void setConditionRule(String conditionRule);

    String getConditionRule();
}
