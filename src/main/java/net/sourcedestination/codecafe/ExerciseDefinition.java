package net.sourcedestination.codecafe;

import java.util.Collection;
import java.util.stream.Stream;

public class ExerciseDefinition {

    private final Collection<Restriction> restrictions;
    private final Collection<Goal> goals;
    private final String id;
    private final String template;
    private final long timeout;

    public ExerciseDefinition(String exerciseId, long timeout,
                              String template,
                              Collection<Restriction> restrictions,
                              Collection<Goal> goals) {
        this.goals = goals;
        this.restrictions = restrictions;
        this.id = exerciseId;
        this.timeout = timeout;
        this.template = template;
    }

    public String getId() { return id; }
    public String getTemplate() { return template; }
    public long getTimeout() { return timeout; }
    public Stream<Goal> getGoals() { return goals.stream(); }
    public Stream<Restriction> getRestrictions() { return restrictions.stream(); }
}