package net.sourcedestination.codecafe;

import jdk.jshell.Snippet;
import net.sourcedestination.funcles.predicate.Predicate2;

public interface Restriction extends Predicate2<Snippet,JShellExerciseTool> {
    /** explanation for a restricted snippet */
    public String getReason();
}
