package net.sourcedestination.codecafe;

import jdk.jshell.SnippetEvent;
import net.sourcedestination.funcles.predicate.Predicate2;

public interface Restriction extends Predicate2<SnippetEvent,JShellExerciseTool> {
    /** explanation for a restricted snippet */
    public String getReason();
}
