package net.sourcedestination.codecafe.structure.restrictions;

import net.sourcedestination.codecafe.InMemoryDBManager;
import net.sourcedestination.codecafe.TestMessagingTemplate;
import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import net.sourcedestination.funcles.tuple.Tuple;
import net.sourcedestination.funcles.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import jdk.jshell.Snippet;

import java.util.Collections;

public class RestrictionTests {
    @Test public void testAcceptableExpressionSnippet() {
        var tool = new JShellExerciseTool("testuser", "1",
                new InMemoryDBManager(),
                1000,
                new TestMessagingTemplate(),
                new ExerciseDefinition(
                        "1", "desc", 1000, "",
                        Collections.emptyList(), new GoalStructure("", "",
                        new Goal("1") {
                            @Override
                            public String getType() {
                                return "fake goal";
                            }

                            @Override
                            public String getDescription() {
                                return "fake goal";
                            }

                            @Override
                            public Tuple2<Double, String> completionPercentage(JShellExerciseTool tool) {
                                return Tuple.makeTuple(0.0, "fake");
                            }
                        })
                ));
        var restriction = new SnippetTypeWhiteList(Snippet.Kind.EXPRESSION);
        var code = "5 + 1 == 3";
        var snippet = tool.getShell().sourceCodeAnalysis().sourceToSnippets(code);
        Assert.assertFalse(restriction.test(snippet.get(0), tool));
    }
}

