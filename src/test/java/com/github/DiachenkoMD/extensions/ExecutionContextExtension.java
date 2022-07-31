package com.github.DiachenkoMD.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

public class ExecutionContextExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if(StateStore.tdr != null)
            return ConditionEvaluationResult.disabled(StateStore.tdr.getReason());

        return ConditionEvaluationResult.enabled("Test enabled.");
    }
}
