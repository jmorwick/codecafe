package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class MethodDefinitionReturnType extends Goal {

    private final String methodName;
    private final String returnType;

    public static String parseSignatureReturnType(String signature) {
        return signature.split("\\)")[1].trim();
    }

    public MethodDefinitionReturnType(String methodName, String signature) {
        super("define-method-named-"+methodName+
                "-with-return-type-"+parseSignatureReturnType(signature));
        this.methodName = methodName;
        this.returnType = parseSignatureReturnType(signature);
    }

    public String getType() { return "method-definition"; }

    public String getDescription() {
        return "A method with the name '"+methodName+"' must be defined with return type "+".";
    }

    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        var js = tool.getShell();

        // check method name exists
        if(!js.methods().anyMatch(m -> m.name().equals(methodName)))
            return makeTuple(0.0, "Method name is not correct");

        // check method has correct return type
        if(!js.methods().anyMatch(m -> m.name().equals(methodName) &&
                parseSignatureReturnType(m.signature()).equals(returnType)))
            return makeTuple(0.0, "incorrect return type, should be " + returnType);

        return makeTuple(1.0, "test passed!");
    }
}
