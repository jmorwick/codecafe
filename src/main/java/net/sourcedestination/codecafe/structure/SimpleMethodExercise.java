package net.sourcedestination.codecafe.structure;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.MethodUnitTest;
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
                convertToGoals(methodName, signature, visibleTests, hiddenTests));

        this.methodName = methodName;
        this.signature = signature;
    }

    public String getMethodName() { return methodName; }
    public String getSignature() { return  signature; }

    public static Collection<Goal> convertToGoals(
            String methodName,
            String signature,
            Collection<Pair<String>> visibleTests,
            Collection<Pair<String>> hiddenTests) {
        List<Goal> goals = new ArrayList<>();

        int testNumber = 1;
        for(Pair<String> test : visibleTests) {
            logger.info("read test: " + test);
            var unitTest = new MethodUnitTest(
                    List.of("unit test " + (testNumber++)),
                    methodName, false, signature, test._2, test._1);
            logger.info(unitTest.getInputs() + " -> " + unitTest.getOutput());
            goals.add(unitTest);
        }
        for(Pair<String> test : hiddenTests)
            goals.add(new MethodUnitTest(
                    List.of("unit test " + (testNumber++)),
                    methodName, true,  signature, test._2, test._1));
        return goals;
    }
}