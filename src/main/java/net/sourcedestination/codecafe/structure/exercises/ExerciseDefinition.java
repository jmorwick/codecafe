package net.sourcedestination.codecafe.structure.exercises;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExerciseDefinition {

    private final Collection<Restriction> restrictions;
    private final GoalStructure goals;
    private final String id;
    private final String template;
    private final String description;
    private final long timeout;

    public ExerciseDefinition(String exerciseId,
                              String description,
                              long timeout,
                              String template,
                              Collection<Restriction> restrictions,
                              GoalStructure goals) {
        this.goals = goals;
        this.description = description;
        this.restrictions = restrictions;
        this.id = exerciseId;
        this.timeout = timeout;
        this.template = template;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getTemplate() { return template; }
    public long getTimeout() { return timeout; }
    public GoalStructure getGoalStructure() { return goals; }
    public Stream<Restriction> getRestrictions() { return restrictions.stream(); }

    /** initializes the state of the JShell tool for this exercise. By default, does nothing */
    public void initializeTool(JShellExerciseTool tool) {}
    /** preprocesses user code snippet before it is evaluated. By default, does nothing */
    public String preprocessSnippet(JShellExerciseTool tool, String snippet) { return snippet; }

    public static String loadTextFile(String fileName) throws IOException {
        var fileString = new BufferedReader(new InputStreamReader(
                ExerciseDefinition.class.getClassLoader()
                        .getResourceAsStream(fileName)))
                .lines().collect(Collectors.joining("\n"));
        return fileString;
    }
}
