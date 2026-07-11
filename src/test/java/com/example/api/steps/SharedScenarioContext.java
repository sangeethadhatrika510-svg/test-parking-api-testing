package com.example.api.steps;

import com.example.api.context.ScenarioContext;

final class SharedScenarioContext {
    private static final ThreadLocal<ScenarioContext> CONTEXT =
            ThreadLocal.withInitial(ScenarioContext::new);

    private SharedScenarioContext() {
    }

    static ScenarioContext context() {
        return CONTEXT.get();
    }
}
