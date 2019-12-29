package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellJavaTool;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;

import java.util.logging.Logger;

public class MethodUnitTest extends ExecutionGoal<JShellJavaTool> {

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
        super(id, hiddenTest ? "<hidden test>" :
                methodName+"("+inputs+") -> " + output);
        this.hiddenTest = hiddenTest;
        this.methodName = methodName;
        this.signature = signature;
        this.output = output;
        this.inputs = inputs;
    }

    @Override
    public String getType() { return "unit-test"; }

    @Override
    public String getLongDescription() {
        return hiddenTest ? "<hidden test>" :
                "The method '" + methodName + "' should return " + output +
                        " when given inputs: " + inputs;
    }

    public String getInputs() { return inputs; }
    public String getOutput() { return output; }

    @Override
    public GoalState evaluateGoal(JShellJavaTool tool) {
        // check test
        var actualOutput = tool.executeRawCode(methodName+"("+inputs+")");
        if(actualOutput.size() != 1)
            return new GoalState(this, "no output", 0, false);
        if(actualOutput.get(0).getStatus() != SnippetExecutionEvent.ExecutionStatus.SUCCESS)
            return new GoalState(this, "invalid result", 0, false);

        if(!valuesMatch(output, actualOutput.get(0).getResult()))
            return new GoalState(this,"incorrect output: " + actualOutput.get(0).getResult(), 0.25, false);

        return new GoalState(this, "test passed!", 1, false);
    }

    public boolean valuesMatch(String v1, String v2) {
        assert v1 != null;
        if(v2 == null) return false;
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
