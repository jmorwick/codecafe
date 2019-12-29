package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.LanguageExecutionTool;

public abstract class ExecutionGoal<T extends LanguageExecutionTool> extends Goal {

    public ExecutionGoal(String id, String description) {
        super(id, description);
    }

    public abstract GoalState evaluateGoal(T tool);
}
