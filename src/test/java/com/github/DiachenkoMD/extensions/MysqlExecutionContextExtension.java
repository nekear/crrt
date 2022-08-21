package com.github.DiachenkoMD.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MysqlExecutionContextExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if(StateStore.mysqlTestsBlocked != null)
            return ConditionEvaluationResult.disabled(StateStore.mysqlTestsBlocked.getReason());

        return ConditionEvaluationResult.enabled("Test enabled.");
    }
}
