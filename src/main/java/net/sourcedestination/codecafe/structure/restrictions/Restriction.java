package net.sourcedestination.codecafe.structure.restrictions;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.predicate.Predicate2;

public interface Restriction extends Predicate2<Snippet,JShellExerciseTool> {
    /** explanation for a restricted snippet */
    public String getReason(Snippet s, JShellExerciseTool tool);
}
