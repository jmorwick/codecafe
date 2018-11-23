package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

public interface Goal {
    public String getType();
    public String getDescription();
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool);
}
