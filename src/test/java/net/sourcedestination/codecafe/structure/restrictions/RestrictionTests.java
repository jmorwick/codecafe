package net.sourcedestination.codecafe.structure.restrictions;

import jdk.jshell.Snippet;
import net.sourcedestination.codecafe.InMemoryDBManager;
import net.sourcedestination.codecafe.TestGoal;
import net.sourcedestination.codecafe.TestMessagingTemplate;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Collections;

public class RestrictionTests {

    private JShellExerciseTool tool =
            new JShellExerciseTool("testuser", "1",
            new InMemoryDBManager(),
            1000,
            new TestMessagingTemplate(),
            new ExerciseDefinition(
                    "1", "desc", 1000, "",
                    Collections.emptyList(), new GoalStructure("", "",
                    new TestGoal())
            ));

    @Test public void testClassExpressionSnippet() {
        var restriction = new SnippetTypeWhiteList(Snippet.Kind.EXPRESSION);
        var code = "class Foo { };";
        var snippets = tool.getShell().sourceCodeAnalysis().sourceToSnippets(code);
        assertTrue(restriction.test(snippets.get(0), tool));
    }

    @Test public void testErroneousExpressionSnippet() {
        var restriction = new SnippetTypeWhiteList(Snippet.Kind.ERRONEOUS);
        // Correctly Filtered It
        var code = "SELECT * FROM testUser Where testUser = ' ' OR '1' = '1'";
        var snippet = tool.getShell().sourceCodeAnalysis().sourceToSnippets(code);
        assertFalse(restriction.test(snippet.get(0), tool));
        assertFalse(true);
    }

    @Test public void testAcceptableExpressionSnippet() {
        var restriction = new SnippetTypeWhiteList(Snippet.Kind.EXPRESSION);
        var code = "5 + 1 == 3";
        var snippet = tool.getShell().sourceCodeAnalysis().sourceToSnippets(code);
        assertFalse(restriction.test(snippet.get(0), tool));
    }
}

