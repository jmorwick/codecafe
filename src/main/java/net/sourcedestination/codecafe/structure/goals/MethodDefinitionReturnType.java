package net.sourcedestination.codecafe.structure.goals;

import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import java.util.List;

public class MethodDefinitionReturnType extends EvaluationGoal<Snippet> {

    private final String methodName;
    private final String returnType;

    public static String parseSignatureReturnType(String signature) {
        return signature.split("\\)")[1].trim();
    }

    public MethodDefinitionReturnType(String methodName, String signature) {
        super(getId(methodName, signature),
                "Method '"+methodName+"' returns a(n) '"+parseSignatureReturnType(signature)+"'");
        this.methodName = methodName;
        this.returnType = parseSignatureReturnType(signature).trim();
    }

    public static String getId(String methodName, String signature) {
        return "define-method-named-"+methodName+
                "-with-return-type-"+parseSignatureReturnType(signature);
    }

    @Override
    public String getType() { return "method-definition"; }

    @Override
    public String getLongDescription() {
        return "A method with the name '"+methodName+"' must be defined with return type '"+ returnType+"'.";
    }

    @Override
    public GoalState evaluateArtifacts(List<Snippet> snippets) {
        if(snippets.size() != 1)
            return new GoalState(this,
                    "only one method definition expected",
                    0,
                    true);

        if(snippets.get(0).subKind() != Snippet.SubKind.METHOD_SUBKIND)
            return new GoalState(this,
                        "a method definition is expected",
                        0,
                        true);
        var methodSnippet = (MethodSnippet)snippets.get(0);
        var actualReturnType = parseSignatureReturnType(methodSnippet.signature()).trim();
        if(actualReturnType.equals(returnType))
            return new GoalState( this,"test passed!",1.0,false);
        else
            return new GoalState(this,"incorrect return type, should be " + returnType +
                    ", not " + actualReturnType,0.0,false);
    }
}
