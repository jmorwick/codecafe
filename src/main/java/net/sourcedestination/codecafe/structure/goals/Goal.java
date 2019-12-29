package net.sourcedestination.codecafe.structure.goals;

import java.util.Map;

public class Goal {
    final private String id;
    final private String description;

    public Goal(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    /** flat string identifying how the client should handle this goal */
    public String getType() { return "generic"; }

    /** A concise description (less than a sentence) of this goal
     * Assume the user already understands the type and category of the goal, so this should distinguish
     * the goal from other goals in the same category.
     * @return
     */
    public String getDescription() {
        return description;
    }

    /** A detailed description of the goal including guidance and/or hints.
     * Optional to implement -- by default returns an empty string which is ignored by the client
     * @return
     */
    public String getLongDescription() {
        return "";
    }

    public Map<String,Object> toStateMap() {
        return Map.of(
                "id", getId(),
                "description", getDescription(),
                "longDescription", getLongDescription(),
                "children", false
        );
    }
}
