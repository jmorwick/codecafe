package net.sourcedestination.codecafe.structure.goals;

import com.google.common.collect.Sets;
import jdk.jshell.Snippet;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;

public class SnippetTypeWhiteList extends EvaluationGoal<Snippet> {

    private final Set<Snippet.Kind> kinds;

    public SnippetTypeWhiteList(String id, Snippet.Kind ... kinds) {
        super(id, "Snippets must be one of the following types: " + kinds);
        this.kinds = Sets.newHashSet(kinds);
    }

    @Override
    public GoalState evaluateArtifacts(List<Snippet> snippets) {
        var offender = StreamEx.of(snippets)
            .findFirst(s ->
                    (s.subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND && !kinds.contains(s.kind())) ||
                    (s.subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND && !kinds.contains(Snippet.Kind.EXPRESSION))
        );

        if(offender.isPresent())
            return new GoalState(this,
                "Snippet was a " + offender.get().kind() + ", must be a " + kinds,
                0.0,
                    true
                );
        else return new GoalState(this, "passed!", 1.0, false);
    }
}
