package net.sourcedestination.codecafe.networking;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.execution.ToolListener;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;

public class JShellExerciseConnection implements ToolListener {
    private ExerciseDefinition exercise;
    private String username;
    private JShellExerciseTool tool;

    public JShellExerciseConnection(String username, ExerciseDefinition exercise, JShellExerciseTool tool) {
        this.username = username;
        this.exercise = exercise;
        this.tool = tool;
    }
}
