package net.sourcedestination.codecafe.structure.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoalStructure extends Goal {

    private final Collection<GoalStructure> substructures;
    private final Collection<? extends Goal> goals;

    public GoalStructure(String name,
                         String description,
                         Collection<GoalStructure> substructures,
                         Collection<? extends Goal> goals) {
        super(name, description);
        this.substructures = substructures;
        this.goals = goals;
    }

    public Collection<? extends Goal> getLeafGoals() {
        return goals;
    }

    public Collection<GoalStructure> getSubstructures() {
        return substructures;
    }

    public Collection<? extends Goal> getAllGoals() {
        List<Goal> allGoals = new ArrayList<>();
        allGoals.addAll(substructures);
        allGoals.addAll(goals);
        return allGoals;
    }

    public Map<String,Object> toStateMap() {
        return Map.of(
                "id", getId(),
                "description", getDescription(),
                "longDescription", getLongDescription(),
                "progress", "0%",
                "reason", "",
                "children", getAllGoals().stream()
                        .map(goal -> goal.toStateMap())
                        .collect(Collectors.toList())
        );
    }
}
