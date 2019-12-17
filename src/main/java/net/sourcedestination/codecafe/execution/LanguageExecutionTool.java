package net.sourcedestination.codecafe.execution;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;

import java.util.List;
import java.util.function.Function;

public interface LanguageTool {
    /** executes code snippet without any transformations, restrictions, goal analysis. */
    public SnippetExecutionEvent executeRawCodeSnippet(String snippet);

    /** applies the transformation, evaluates code restrictions, executes the snippet, and evaluates goals.*/
    public SnippetExecutionEvent executeUserCodeSnippet(String snippet,
                                                        Function<String,String> transformation,
                                                        List<Restriction> restrictions,
                                                        List<Goal> goals);

    /** evaluates code restrictions, executes the snippet, and evaluates goals.*/
    public default SnippetExecutionEvent executeUserCodeSnippet(String snippet,
                                                        List<Restriction> restrictions,
                                                        List<Goal> goals) {
        return executeUserCodeSnippet(snippet, x -> x, restrictions, goals);
    }
}
