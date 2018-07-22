package net.sourcedestination.codecafe;

import net.sourcedestination.funcles.tuple.Tuple2;

public interface Goal {
    public String getDescription();
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool);
}
