package net.sourcedestination.codecafe.structure.goals;

import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;

import java.util.Arrays;
import java.util.List;

public class MethodDefinitionParameters extends EvaluationGoal<Snippet> {

    private final String methodName;
    private final String[] params;

    public static String[] parseSignatureParameters(String signature) {
        return signature.split("\\)")[0].trim().substring(1).split(",");
    }

    public MethodDefinitionParameters(String methodName, String signature) {
        super("define-method-named-"+methodName+
                "-with-parameters-"+parseSignatureParameters(signature),
                "Method '"+methodName+"' has " +
                        (parseSignatureParameters(signature).length == 0 ?
                                "no parameters" :
                                "parameters with types ("+
                                        Arrays.toString(parseSignatureParameters(signature))+
                                        ")"));
        this.methodName = methodName;
        this.params = parseSignatureParameters(signature);
    }

    public static String getId(String methodName, String signature) {
        return "define-method-named-"+methodName+
                "-with-parameters-"+parseSignatureParameters(signature);
    }

    @Override
    public String getType() { return "method-definition"; }

    @Override
    public String getLongDescription() {
        return "A method with the name '"+methodName+"' must be defined with " +
                params.length + " parameters with type" +
                (params.length > 1 ? "s" : "") + // pluralize
                ": "+ Arrays.toString(params)+".";
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

        var actualParams = parseSignatureParameters(methodSnippet.signature());
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
            return new GoalState( this,"test passed!",1.0,false);
        else
            return new GoalState(this,mistakes,
                    (actualParams.length == params.length ? 0.25 : 0.0) + // 25% for right # of params
                            ((double)correct / actualParams.length)*0.5,          // 50% for correct param types
                     false);
    }

}
