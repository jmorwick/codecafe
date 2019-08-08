package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GoalStructure extends Goal {

    private String shortDescription;
    private String longDescription;
    private List<Goal> subgoals;

    public GoalStructure(String shortDescription,
                         String longDescription,
                         Goal ... subgoals) {
        super("goal-structure"); // ID is ignored
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.subgoals = Arrays.asList(subgoals);
        if(subgoals.length == 0)
            throw new IllegalArgumentException("Must have at least one subgoal");
    }

    @Override
    public String getType() {
        return getId();
    }

    @Override
    public String getDescription() {
        return shortDescription;
    }

    @Override
    public String getLongDescription() {
        return longDescription;
    }

    @Override
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        return Tuple.makeTuple(
                subgoals.stream() // calculate average of subgoals
                        .mapToDouble(goal -> goal.completionPercentage(tool)._1)
                        .average().getAsDouble(),
                "category-average"
        );
    }

    /** returns a stream of all goals that are updated by changes to the jshell state */
    public Stream<Goal> getLeafGoals() {
        return subgoals.stream().flatMap(subgoal ->
                subgoal instanceof GoalStructure ?
                    ((GoalStructure) subgoal).getLeafGoals() :
                    Stream.of(subgoal)
        );
    }
}
