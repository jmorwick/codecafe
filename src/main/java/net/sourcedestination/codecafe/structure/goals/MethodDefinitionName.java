package net.sourcedestination.codecafe.structure.goals;

import jdk.jshell.Snippet;

import java.util.List;

public class MethodDefinitionName extends EvaluationGoal<Snippet> {

    private final String methodName;

    public MethodDefinitionName(String methodName) {
        super(getId(methodName), "Define method '"+methodName+"'");
        this.methodName = methodName;
    }

    public static String getId(String methodName) {
        return "define-method-named-"+methodName;
    }

    @Override
    public String getType() { return "method-definition"; }

    @Override
    public String getLongDescription() {
        return "A method with the name '"+methodName+"' must be defined.";
    }

    @Override
    public GoalState evaluateArtifacts(List<Snippet> snippets) {
        if(snippets.size() != 1)
            return new GoalState(this,
                "only one method definition expected",
                0,
                true);
        var methodSnippet = snippets.get(0);
        if(methodSnippet.subKind() != Snippet.SubKind.METHOD_SUBKIND)
            return new GoalState(this,
                        "a method definition is expected",
                        0,
                        true);

        if(!methodSnippet.id().equals(methodName))
            return new GoalState( this,"test passed!",1.0,false);
        else
            return new GoalState(this,"Method name is not correct",0.0,false);
    }
}
