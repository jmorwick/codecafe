
package net.sourcedestination.codecafe.structure.restrictions;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.InMemoryDBManager;
import net.sourcedestination.codecafe.TestGoal;
import net.sourcedestination.codecafe.TestMessagingTemplate;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class RestrictionTests {

    public static List<String> EXPRESSIONS = List.of("5",  "2 + 5");
    public static List<String> VARIABLE_DEFINITIONS = List.of(
            "int add(int x, int y) { return x + y; }",
            "void doNothing() {}"
    );
    private List<Snippet.Kind> whiteList;
    private List<Snippet> validSnippets;
    private List<Snippet> invalidSnippets;

    public RestrictionTests(List<Snippet.Kind> whiteList,
                            List<String> validSnippets,
                            List<String> invalidSnippets) {
        this.whiteList = whiteList;

        tool =
                new JShellExerciseTool("testuser", "1",
                        new InMemoryDBManager(),
                        1000,
                        new TestMessagingTemplate(),
                        new ExerciseDefinition(
                                "1", "desc", 1000, "",
                                Collections.emptyList(), new GoalStructure("", "",
                                new TestGoal())
                        ));

        this.validSnippets = validSnippets.stream()
                .map(s -> tool.getShell().sourceCodeAnalysis().sourceToSnippets(s).get(0))
                .collect(Collectors.toList());
        this.invalidSnippets = invalidSnippets.stream()
                .map(s -> tool.getShell().sourceCodeAnalysis().sourceToSnippets(s).get(0))
                .collect(Collectors.toList());
    }

    @Parameters public static Collection<Object[]> parameters() {
        return List.of(
                new Object[]{
                        List.of(Snippet.Kind.EXPRESSION),
                        VARIABLE_DEFINITIONS,
                        EXPRESSIONS},
                new Object[]{
                        List.of(Snippet.Kind.METHOD),
                        EXPRESSIONS,
                        VARIABLE_DEFINITIONS}
        );
    }

    private JShellExerciseTool tool;

    @Test public void testValidSnippets() {
        var restriction = new SnippetTypeWhiteList(whiteList.toArray(new Snippet.Kind[0]));
        validSnippets.forEach(s ->
                assertTrue(s.kind() + " should be filtered for whitelist " + whiteList + ": " + s.source(),
                        restriction.test(s, tool)));
    }

    @Test public void testInvalidSnippets() {
        var restriction = new SnippetTypeWhiteList(whiteList.toArray(new Snippet.Kind[0]));
        invalidSnippets.forEach(s -> {
                assertFalse(s.kind() + " should not be filtered for whitelist " + whiteList + ": " + s.source(),
                        restriction.test(s, tool));
        });
    }

}
