package net.sourcedestination.codecafe.structure.exercises;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ExerciseDefinition {

    private final Collection<Restriction> restrictions;
    private final GoalStructure goals;
    private final String id;
    private final String template;
    private final long timeout;

    public ExerciseDefinition(String exerciseId, long timeout,
                              String template,
                              Collection<Restriction> restrictions,
                              GoalStructure goals) {
        this.goals = goals;
        this.restrictions = restrictions;
        this.id = exerciseId;
        this.timeout = timeout;
        this.template = template;
    }

    public String getId() { return id; }
    public String getTemplate() { return template; }
    public long getTimeout() { return timeout; }
    public GoalStructure getGoalStructure() { return goals; }
    public Stream<Restriction> getRestrictions() { return restrictions.stream(); }

    /** initializes the state of the JShell tool for this exercise. By default, does nothing */
    public void initializeTool(JShellExerciseTool tool) {}
    /** preprocesses user code snippet before it is evaluated. By default, does nothing */
    public String preprocessSnippet(JShellExerciseTool tool, String snippet) { return snippet; }

}
