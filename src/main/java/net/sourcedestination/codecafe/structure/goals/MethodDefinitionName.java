package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class MethodDefinitionName {

    private final String methodName;

    public MethodDefinitionName(String methodName) {
        this.methodName = methodName;
    }

    public String getType() { return "method-definition"; }

    public String getDescription() {
        return "A method with the name '"+methodName+"' must be defined.";
    }

    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return makeTuple(0.0, "Method name is not correct");
        else {
            return makeTuple(1.0, "test passed!");
        }
    }
}
