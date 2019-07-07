package net.sourcedestination.codecafe.structure.goals;

import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.List;
import java.util.logging.Logger;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class MethodUnitTest extends Goal {

    private static final Logger logger = Logger.getLogger(MethodUnitTest.class.getCanonicalName());

    private final String methodName;
    private final boolean hiddenTest;
    private final String signature;
    private final String inputs;
    private final String output;

    public MethodUnitTest(String id,
                          String methodName,
                          boolean hiddenTest,
                          String signature,
                          String output,
                          String inputs) {
        super(id);
        this.hiddenTest = hiddenTest;
        this.methodName = methodName;
        this.signature = signature;
        this.output = output;
        this.inputs = inputs;
    }

    public String getType() { return "unit-test"; }

    public String getDescription() {
        return hiddenTest ? "<hidden test>" :
                "The method should be named '" + methodName + "' and have parameters " +
                        signature + " and should return " + output + " when given inputs: " + inputs;
    }

    public String getInputs() { return inputs; }
    public String getOutput() { return output; }

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
        if(actualOutput.size() < 1)
            return makeTuple(0.5, "no output recieved");
        if(actualOutput.get(0).status() != Snippet.Status.VALID )
            return makeTuple(0.5, "invalid result");
        if(actualOutput.get(0).snippet().subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND)
            return makeTuple(0.5, "wrong type of result");

        var actualTextOutput = js.varValue((VarSnippet)actualOutput.get(0).snippet());
        if(!output.equals(actualTextOutput))
            return makeTuple(0.5, "incorrect output: " + actualTextOutput);

        return makeTuple(1.0, "test passed!");
    }
}
