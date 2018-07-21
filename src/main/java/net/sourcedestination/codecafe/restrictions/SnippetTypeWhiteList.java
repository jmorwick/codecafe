package net.sourcedestination.codecafe.restrictions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.JShellExerciseTool;
import net.sourcedestination.codecafe.Restriction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SnippetTypeWhiteList implements Restriction {

    private final Set<Snippet.Kind> kinds;

    public SnippetTypeWhiteList(Snippet.Kind ... kinds) {
        this.kinds = Sets.newHashSet(kinds);
    }

    @Override
    public boolean test(Snippet s, JShellExerciseTool tool) {
        if(s.subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND)
            return !kinds.contains(Snippet.Kind.EXPRESSION);

        return !kinds.contains(s.kind());
    }

    @Override
    public String getReason(Snippet s, JShellExerciseTool tool) {
        // TODO: improve language
        return "Snippet was a " + s.kind() + ", must be a " + kinds;
    }
}
