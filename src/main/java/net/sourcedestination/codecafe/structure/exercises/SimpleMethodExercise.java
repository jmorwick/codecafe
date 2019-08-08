package net.sourcedestination.codecafe.structure.exercises;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.structure.goals.*;
import net.sourcedestination.codecafe.structure.restrictions.SnippetTypeWhiteList;
import net.sourcedestination.funcles.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class SimpleMethodExercise extends ExerciseDefinition {


    private static final Logger logger = Logger.getLogger(SimpleMethodExercise.class.getCanonicalName());

    private String methodName;
    private String signature;

    public SimpleMethodExercise(String exerciseId,
                                String methodName,
                                long timeout,
                                String signature,
                                String description,
                                Collection<Pair<String>> visibleTests,
                                Collection<Pair<String>> hiddenTests) {
        super(exerciseId,
                timeout,
                "simple-method",
                List.of(new SnippetTypeWhiteList(Snippet.Kind.METHOD)),
                convertToGoalStructure(methodName, signature, visibleTests, hiddenTests));

        this.methodName = methodName;
        this.signature = signature;
    }

    public String getMethodName() { return methodName; }
    public String getSignature() { return  signature; }

    public static GoalStructure convertToGoalStructure(
            String methodName,
            String signature,
            Collection<Pair<String>> visibleTests,
            Collection<Pair<String>> hiddenTests) {

        int testNumber = 1;
        List<Goal> visibleTestGoals = new ArrayList<>();
        for(Pair<String> test : visibleTests) {
            logger.info("read test: " + test);
            var unitTest = new MethodUnitTest(
                    "unit test " + (testNumber++),
                    methodName, false, signature, test._2, test._1);
            logger.info(unitTest.getInputs() + " -> " + unitTest.getOutput());
            visibleTestGoals.add(unitTest);
        }
        List<Goal> hiddenTestGoals = new ArrayList<>();
        for(Pair<String> test : hiddenTests)
            hiddenTestGoals.add(new MethodUnitTest(
                    "unit test " + (testNumber++),
                    methodName, true,  signature, test._2, test._1));

        return new GoalStructure(
                "All goals",
                "Goals for expression submission",
                new GoalStructure("Method Definition",
                        "The header of the method is properly defined",
                        new MethodDefinitionName(methodName),
                        new MethodDefinitionReturnType(methodName,signature),
                        new MethodDefinitionParameters(methodName,signature)),
                new GoalStructure("Visible Tests", "Visible Tests",
                        visibleTestGoals.toArray(new Goal[0])),
                new GoalStructure("Hidden Tests", "Hidden Tests",
                        hiddenTestGoals.toArray(new Goal[0]))
        );    }
}