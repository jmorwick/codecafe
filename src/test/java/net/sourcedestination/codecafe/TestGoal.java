package net.sourcedestination.codecafe;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.funcles.tuple.Tuple2;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class TestGoal extends Goal {
    public TestGoal() {
        super("test");
    }
    public String getDescription() { return "test goal"; }
    public String getType() { return "test goal"; }
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool ){
        return makeTuple(0.0, "");
    }
}
