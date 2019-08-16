package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.Arrays;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class MethodDefinitionParameters extends Goal {

    private final String methodName;
    private final String[] params;

    public static String[] parseSignatureParameters(String signature) {
        return signature.split("\\)")[0].trim().substring(1).split(",");
    }

    public MethodDefinitionParameters(String methodName, String signature) {
        super("define-method-named-"+methodName+
                "-with-parameters-"+parseSignatureParameters(signature));
        this.methodName = methodName;
        this.params = parseSignatureParameters(signature);
    }

    @Override
    public String getType() { return "method-definition"; }

    @Override
    public String getDescription() {
        return "Method '"+methodName+"' has " +
                (params.length == 0 ?
                        "no parameters" :
                        "parameters with types ("+ Arrays.toString(params)+")");
    }

    @Override
    public String getLongDescription() {
        return "A method with the name '"+methodName+"' must be defined with " +
                params.length + " parameters with type" +
                (params.length > 1 ? "s" : "") + // pluralize
                ": "+ Arrays.toString(params)+".";
    }

    @Override
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return makeTuple(0.0, "Method name is not correct");
        else {
            var actualParams = parseSignatureParameters(
                    js.methods().filter(m -> m.name().equals(methodName)) // find method definition
                            .findFirst().get()        // exactly one should exist
                            .signature());  // retrieve and parse its signature
            // check method has correct parameters
            var correct = 0;
            var mistakes = "";
            for(int i=0; i<params.length; i++) {
                if(i >= actualParams.length) {
                    mistakes += "Parameter #" + (i + 1) + " was not defined. ";
                } else if(!actualParams[i].equals(params[i])){
                    mistakes += "Parameter #" + (i + 1) + " should have been " + params[i] +
                            " but was " + actualParams[i];
                } else {
                    correct++;
                }
            }

            if(mistakes.equals("") && actualParams.length == params.length)
                return makeTuple(1.0, "All tests passed!");

            return makeTuple(
                            (actualParams.length == params.length ? 0.25 : 0.0) + // 25% for right # of params
                            ((double)correct / actualParams.length)*0.5,          // 50% for correct param types
                    mistakes);
        }
    }
}
