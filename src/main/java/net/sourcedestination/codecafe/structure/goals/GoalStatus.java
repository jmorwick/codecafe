package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;

import java.util.List;

/** communicates the status of a goal to a user
 *
 */
public class GoalStatus {
    private final double completionPercentage;
    private final String message;
    private final String detailedMessage;
    private final JShellExerciseTool tool;
    private final Goal goal;

    public GoalStatus(JShellExerciseTool tool, Goal goal,
                      double completionPercentage, String message, String detailedMessage) {
        this.completionPercentage = completionPercentage;
        this.message = message;
        this.detailedMessage = detailedMessage;
        this.tool = tool;
        this.goal = goal;
    }

    public List<String> getId() { return tool.getGoalId(goal); }
    public double getCompletionPercentage() { return completionPercentage; }
    public String getMessage() { return message; }
    public String getDetailedMessage() { return detailedMessage; }
}
