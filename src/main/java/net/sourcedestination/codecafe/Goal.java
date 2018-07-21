package net.sourcedestination.codecafe;

public interface Goal {
    public String getDescription();
    public double completionPercentage(JShellExerciseTool tool);
}
