package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;

public interface Goal {
    /** flat string identifying how the client should handle this goal */
    public String getType();

    /** A concise description (less than a sentence) of this goal
     * Assume the user already understands the type and category of the goal, so this should distinguish
     * the goal from other goals in the same category.
     * @return
     */
    public String getDescription();


    /** A detailed description of the goal including guidance and/or hints.
     * Optional to implement -- by default returns an empty string which is ignored by the client
     * @return
     */
    public default String getLongDescription() {
        return "";
    }

    /** Describes the progress towards meeting this goal as of the last evaluated code snippet.
     * @param tool
     * @return
     */
    public GoalStatus getStatus(JShellExerciseTool tool);
}
