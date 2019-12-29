package net.sourcedestination.codecafe.structure.goals;

import java.util.Map;

public class GoalState {
    private final Goal goal;
    private final String reason;
    private final double progress;
    private final boolean fatal;

    public GoalState(Goal goal, String reason, double progress, boolean fatal) {
        this.goal = goal;
        this.reason = reason;
        this.progress = progress;
        this.fatal = fatal;
    }

    public Goal getGoal() { return goal; }
    public String getReason() { return reason; }
    public double getProgress() { return progress; }
    public boolean wasFatal() { return fatal; }


    public Map<String,Object> toPropertyMap() {
        return Map.of(
                "id", goal.getId(),
                "description", goal.getDescription(),
                "longDescription", goal.getLongDescription(),
                "progress", getProgress(),
                "fatal", wasFatal(),
                "reason", getReason()
        );
    }

    @Override
    public String toString() {
        return toPropertyMap().toString();
    }
}
