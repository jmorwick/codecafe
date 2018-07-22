package net.sourcedestination.codecafe.goals;

import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sourcedestination.codecafe.Goal;
import net.sourcedestination.codecafe.JShellExerciseTool;

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

    public double completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return 0;

        // check method has correct signature
        if(!js.methods().anyMatch(m -> m.name().equals(methodName) &&
                m.signature().equals(signature)))
            return 0.25;

        // check test
        var output = js.eval(methodName+"("+inputs+")");
        if(output.size() < 1 ||
                output.get(0).status() != Snippet.Status.VALID ||
                output.get(0).snippet().subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND ||
                !output.equals(js.varValue((VarSnippet)output.get(0).snippet())))
            return 0.5;

        return 1.0;
    }
}
