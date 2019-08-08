package net.sourcedestination.codecafe.structure.exercises;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import net.sourcedestination.codecafe.structure.goals.MethodUnitTest;
import net.sourcedestination.codecafe.structure.restrictions.SnippetTypeWhiteList;
import net.sourcedestination.funcles.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExpressionExercise extends ExerciseDefinition {
    private Map<String,Object> parameters;
    private String targetType;

    // TODO: get restrictions working for expressions only (despite preprocessing)

    public ExpressionExercise(String exerciseId,
                              long timeout,
                              Map<String,Object> parameters,
                              String targetType,
                              Collection<Pair<String>> visibleTests,
                              Collection<Pair<String>> hiddenTests) {
        super(exerciseId,
                timeout,
                "develop-an-expression",
                List.of(new SnippetTypeWhiteList(Snippet.Kind.EXPRESSION)),
                convertToGoalStructure(parameters, targetType, visibleTests, hiddenTests));
        this.parameters = parameters;
        this.targetType = targetType;
    }

    public static GoalStructure convertToGoalStructure(
            Map<String,Object> parameters,
            String targetType,
            Collection<Pair<String>> visibleTests,
            Collection<Pair<String>> hiddenTests) {

        var signature =
                "(" + parameters.values().stream()
                .map( value -> determineType(value))
                .reduce((x,y) -> x + ","+y).orElse("")
                +")"+targetType;

        int testNumber = 1;

        var visibleTestGoals = new ArrayList<Goal>();
        for(Pair<String> test : visibleTests) {
            var unitTest = new MethodUnitTest(
                    "unit test " + (testNumber++),
                    "expressionTestMethod", false, signature, test._2, test._1);
            visibleTestGoals.add(unitTest);
        }

        var hiddenTestGoals = new ArrayList<Goal>();
        for(Pair<String> test : hiddenTests)
            hiddenTestGoals.add(new MethodUnitTest(
                    "unit test " + (testNumber++),
                    "expressionTestMethod", true,  signature, test._2, test._1));

        return new GoalStructure(
                "All goals",
                "Goals for expression submission",
                new GoalStructure("Visible Tests", "Visible Tests",
                        visibleTestGoals.toArray(new Goal[0])),
                new GoalStructure("Hidden Tests", "Hidden Tests",
                        hiddenTestGoals.toArray(new Goal[0]))
        );
    }

    public static String determineType(Object value) {
        if(value instanceof Integer) return "int";
        if(value instanceof Double) return "double";
        if(value instanceof Float) return "float";
        if(value instanceof Boolean) return "boolean";
        if(value instanceof Character) return "char";
        if(value instanceof Byte) return "byte";
        return value.getClass().getCanonicalName();
    }

    /** sets all parameter values so they show up in the variable UI for the user */
    @Override
    public void initializeTool(JShellExerciseTool tool) {
        parameters.entrySet().stream()
                .forEach(entry -> tool.directlyExecuteCodeSnippet(
                        "var " + entry.getKey() + "=" + entry.getValue() + ";" ,
                        entry.getValue().toString()));
    }

    /** User's response should be a single expression. Convert it to a method for unit tests */
    @Override
    public String preprocessSnippet(JShellExerciseTool tool, String snippet) {
        tool.reset();
        return targetType + " expressionTestMethod(" +
                parameters.entrySet().stream() // convert params in to parameter definitions
                    .map(entry -> determineType(entry.getValue()) + " " + entry.getKey())
                    .reduce((x,y) -> x + ", " + y).orElse("") +
                ") { return " + snippet + ";}";
    }
}
