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

    @Override
    public String getType() { return "unit-test"; }

    @Override
    public String getDescription() {
        return hiddenTest ? "<hidden test>" :
                methodName+"("+inputs+") -> " + output;
    }

    @Override
    public String getLongDescription() {
        return hiddenTest ? "<hidden test>" :
                "The method '" + methodName + "' should return " + output +
                        " when given inputs: " + inputs;
    }

    public String getInputs() { return inputs; }
    public String getOutput() { return output; }

    @Override
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return makeTuple(0.0, "Method not properly defined");

        // check method has correct signature
        if(!js.methods().anyMatch(m -> m.name().equals(methodName) &&
                m.signature().equals(signature)))
            return makeTuple(0.0, "Method not properly defined");

        // check test
        var actualOutput = js.eval(methodName+"("+inputs+")");
        if(actualOutput.size() < 1)
            return makeTuple(0.0, "no output received");
        if(actualOutput.get(0).status() != Snippet.Status.VALID )
            return makeTuple(0.0, "invalid result");
        if(actualOutput.get(0).snippet().subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND)
            return makeTuple(0.0, "wrong type of result");

        var actualTextOutput = js.varValue((VarSnippet)actualOutput.get(0).snippet());
        if(!valuesMatch(output, actualTextOutput))
            return makeTuple(0.25, "incorrect output: " + actualTextOutput);

        return makeTuple(1.0, "test passed!");
    }

    public boolean valuesMatch(String v1, String v2) {
        if(v1.equals(v2)) return true;
        try {
            var d1 = Math.round(1000*Double.parseDouble(v1));
            var d2 = Math.round(1000*Double.parseDouble(v2));
            if(d1 == d2)  return true;
        } catch(NumberFormatException e) {
            return false;
        }
        return false;
    }
}
