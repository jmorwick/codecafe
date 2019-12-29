package net.sourcedestination.codecafe.execution;

import net.sourcedestination.codecafe.structure.goals.EvaluationGoal;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.List;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public interface LanguageEvaluationTool<A> {
    public List<A> evaluateCode(String source);

    public default Tuple2<
                LanguageEvaluationTool<A>,
                EvaluationGoal<LanguageEvaluationTool<A>>
            >  packageWithGoal(EvaluationGoal<LanguageEvaluationTool<A>> goal) {
        return makeTuple(this, goal);
    }
}
