package net.sourcedestination.codecafe.goals;

import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sourcedestination.codecafe.Goal;
import net.sourcedestination.codecafe.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class MethodUnitTest implements Goal {

    private final String methodName;
    private final boolean hiddenTest;
    private final String signature;
    private final String inputs;
    private final String output;

    public MethodUnitTest(String methodName,
                          boolean hiddenTest,
                          String signature,
                          String output,
                          String inputs) {
        this.hiddenTest = hiddenTest;
        this.methodName = methodName;
        this.signature = signature;
        this.output = output;
        this.inputs = inputs;
    }

    public String getDescription() {
        return hiddenTest ? "<hidden test>" :
                "The method should be named '" + methodName + "' and have parameters " +
                        signature + " and should return " + output + " when given inputs: " + inputs;
    }

    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return makeTuple(0.0, "Method name is not correct");

        // check method has correct signature
        if(!js.methods().anyMatch(m -> m.name().equals(methodName) &&
                m.signature().equals(signature)))
            return makeTuple(0.25, "Parameters and/or return type are not correct");

        // check test
        var actualOutput = js.eval(methodName+"("+inputs+")");
        if(actualOutput.size() < 1 ||
                actualOutput.get(0).status() != Snippet.Status.VALID ||
                actualOutput.get(0).snippet().subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND ||
                !output.equals(js.varValue((VarSnippet)actualOutput.get(0).snippet())))
            return makeTuple(0.5, "incorrect output");

        return makeTuple(1.0, "test passed!");
    }
}
