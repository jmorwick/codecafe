package net.sourcedestination.codecafe.structure.exercises;

import com.google.common.collect.ImmutableMap;
import net.sourcedestination.codecafe.execution.LanguageEvaluationTool;
import net.sourcedestination.codecafe.execution.LanguageExecutionTool;
import net.sourcedestination.codecafe.structure.goals.ExecutionGoal;
import net.sourcedestination.codecafe.structure.goals.EvaluationGoal;
import net.sourcedestination.codecafe.structure.goals.GoalStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExerciseDefinition {

    private final Map<String,EvaluationGoal> evalGoals;
    private final Map<String,ExecutionGoal> execGoals;
    private final Function<String,String> transformation;
    private final String id;
    private final String template;
    private final String description;
    private final long timeout;
    private final GoalStructure goalStructure;

    public ExerciseDefinition(String exerciseId,
                              String description,
                              long timeout,
                              String template,
                              Function<String,String> transformation,
                              Collection<? extends EvaluationGoal> evalGoals,
                              Collection<? extends ExecutionGoal> execGoals,
                              GoalStructure goalStructure) {
        this.evalGoals = ImmutableMap.copyOf(evalGoals.stream().collect(Collectors.toMap(g -> g.getId(), g-> g)));
        this.execGoals = ImmutableMap.copyOf(execGoals.stream().collect(Collectors.toMap(g -> g.getId(), g-> g)));
        this.description = description;
        this.transformation = transformation;
        this.id = exerciseId;
        this.timeout = timeout;
        this.template = template;
        this.goalStructure = goalStructure;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getTemplate() { return template; }
    public long getTimeout() { return timeout; }
    public String transformUserAttempt(String code) { return transformation.apply(code); }
    public Stream<ExecutionGoal> getExecutionGoals() { return execGoals.values().stream(); }
    public Stream<EvaluationGoal> getEvaluationGoals() { return evalGoals.values().stream(); }
    public GoalStructure getGoalStructure() { return goalStructure; }

    public void initializeTool(LanguageEvaluationTool tool) { }
    public void initializeTool(LanguageExecutionTool tool) { }

    public static String loadTextFile(String fileName) throws IOException {
        var fileString = new BufferedReader(new InputStreamReader(
                ExerciseDefinition.class.getClassLoader()
                        .getResourceAsStream(fileName)))
                .lines().collect(Collectors.joining("\n"));
        return fileString;
    }
}
