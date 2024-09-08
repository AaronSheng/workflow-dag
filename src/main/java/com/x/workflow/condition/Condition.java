package com.x.workflow.condition;

import java.util.Map;

public interface Condition {

    public String getConditionName();

    boolean match(String rule, Map<String, Object> contextParameters);
}
