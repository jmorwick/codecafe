package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.LanguageEvaluationTool;

import java.util.List;

public abstract class EvaluationGoal<A> extends Goal {

    public EvaluationGoal(String id, String description) {
        super(id, description);
    }

    public abstract GoalState evaluateArtifacts(List<A> artifacts);

    public GoalState evaluateGoal(String source, LanguageEvaluationTool<A> tool) {
        return evaluateArtifacts(tool.evaluateCode(source));
    }
}
