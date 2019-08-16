package net.sourcedestination.codecafe.structure.exercises;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import net.sourcedestination.codecafe.structure.restrictions.SnippetTypeWhiteList;
import java.util.List;

public class ExpressionExercise extends ExerciseDefinition {
    private String targetType;
    private List<String> initializationCode;

    public ExpressionExercise(String exerciseId,
                                        String description,
                                        long timeout,
                                        List<String> initializationCode,
                                        String targetType,
                                        GoalStructure goals) {
        super(exerciseId,
                description,
                timeout,
                "develop-an-expression",
                List.of(new SnippetTypeWhiteList(Snippet.Kind.EXPRESSION)),
                goals);
        this.initializationCode = initializationCode;
        this.targetType = targetType;
    }


    /** User's response should be a single expression. Convert it to a method for unit tests */
    @Override
    public String preprocessSnippet(JShellExerciseTool tool, String snippet) {
        return targetType + " expressionTestMethod() { return " + snippet + ";}";
    }

    @Override
    public void initializeTool(JShellExerciseTool tool) {
        initializationCode.forEach(tool::silentlyExecuteCodeSnippet);
    }
}
