package net.sourcedestination.codecafe.structure.exercises;

import net.sourcedestination.codecafe.execution.LanguageEvaluationTool;
import net.sourcedestination.codecafe.execution.LanguageExecutionTool;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;
import net.sourcedestination.codecafe.structure.goals.*;
import net.sourcedestination.funcles.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class SimpleMethodExercise extends ExerciseDefinition {


    private static final Logger logger = Logger.getLogger(SimpleMethodExercise.class.getCanonicalName());

    private final String methodName;
    private final String signature;
    private final Collection<String> imports;
    private final Collection<String> staticImports;

    public SimpleMethodExercise(String methodName,
                                String description,
                                String signature,
                                long timeout,
                                Collection<? extends EvaluationGoal> evalGoals,
                                Collection<ExecutionGoal> execGoals,
                                GoalStructure goalStructure,
                                Collection<String> imports,
                                Collection<String> staticImports) {
        super(methodName,
                description,
                timeout,
                "simple-method",
                x->x,
                evalGoals,
                execGoals,
                goalStructure);

        this.methodName = methodName;
        this.signature = signature;
        this.imports = imports;
        this.staticImports = staticImports;
    }


    public static SimpleMethodExercise build(String methodName,
                                             String description,
                                             String signature,
                                             long timeout,
                                             Collection<Pair<String>> visibleTests,
                                             Collection<Pair<String>> hiddenTests) {
        return build(methodName, description, signature, timeout, visibleTests, hiddenTests, List.of(), List.of());
    }
    public static SimpleMethodExercise build(String methodName,
                                             String description,
                                             String signature,
                                             long timeout,
                                             Collection<Pair<String>> visibleTests,
                                             Collection<Pair<String>> hiddenTests,
                                             Collection<String> imports,
                                             Collection<String> staticImports) {

        int testNumber = 1;
        var evalGoals =
                List.of(new MethodDefinitionName(methodName),
                        new MethodDefinitionReturnType(methodName,signature),
                        new MethodDefinitionParameters(methodName,signature));

        List<ExecutionGoal> visibleTestGoals = new ArrayList<>();
        for(Pair<String> test : visibleTests) {
            logger.info("read test: " + test);
            var unitTest = new MethodUnitTest(
                    "unit test " + (testNumber++),
                    methodName, false, signature, test._2, test._1);
            logger.info(unitTest.getInputs() + " -> " + unitTest.getOutput());
            visibleTestGoals.add(unitTest);
        }
        List<ExecutionGoal> hiddenTestGoals = new ArrayList<>();
        for(Pair<String> test : hiddenTests)
            hiddenTestGoals.add(new MethodUnitTest(
                    "unit test " + (testNumber++),
                    methodName, true,  signature, test._2, test._1));

        List<ExecutionGoal> allGoals = new ArrayList<>();
        allGoals.addAll(visibleTestGoals);
        allGoals.addAll(hiddenTestGoals);

        return new SimpleMethodExercise(
                methodName,
                description,
                signature,
                timeout,
                evalGoals,
                allGoals,
                new GoalStructure("All Goals", "All Goals",
                        List.of(
                                new GoalStructure("Method Definition",
                                        "The header of the method is properly defined",
                                        List.of(),
                                        evalGoals
                                ),
                                new GoalStructure("Visible Tests", "Visible Tests",
                                        List.of(),
                                        visibleTestGoals
                                ),
                                new GoalStructure("Hidden Tests", "Hidden Tests",
                                        List.of(),
                                        hiddenTestGoals
                                )
                        ),
                        List.of()
                ),
                imports,
                staticImports
        );

    }

    public String getMethodName() { return methodName; }
    public String getSignature() { return  signature; }

    public String getImports() {
        String snippet = "";
        for(String c : imports) {
            snippet += "import " + c + ";\n";
        }
        for(String c : staticImports) {
            snippet += "import static " + c + ";\n";
        }
        return snippet;
    }

    @Override
    public void initializeTool(LanguageExecutionTool t) {
        for(var result : t.executeRawCode(getImports())) {
            if(result.getStatus() != SnippetExecutionEvent.ExecutionStatus.SUCCESS) {
                throw new IllegalStateException("failed to initialize tool: " + result);
            }
        }
    }

}