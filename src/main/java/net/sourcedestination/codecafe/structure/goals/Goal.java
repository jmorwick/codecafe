package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.Map;

public abstract class Goal {

    private final String id;

    public Goal(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /** flat string identifying how the client should handle this goal */
    public abstract String getType();

    /** A concise description (less than a sentence) of this goal
     * Assume the user already understands the type and category of the goal, so this should distinguish
     * the goal from other goals in the same category.
     * @return
     */
    public abstract String getDescription();


    /** A detailed description of the goal including guidance and/or hints.
     * Optional to implement -- by default returns an empty string which is ignored by the client
     * @return
     */
    public String getLongDescription() {
        return "";
    }

    public abstract Tuple2<Double,String> completionPercentage(JShellExerciseTool tool);


    public Map<String,Object> toStateMap(JShellExerciseTool tool) {
        var completion = completionPercentage(tool);
        return Map.of(
                "id", getId(),
                "description", getDescription(),
                "longDescription", getLongDescription(),
                "progress", completion._1,
                "reason", completion._2,
                "children", false
        );
    }
}
