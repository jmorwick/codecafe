package net.sourcedestination.codecafe.persistance;

import net.sourcedestination.codecafe.structure.goals.GoalState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SnippetExecutionEvent {

    public enum ExecutionStatus {
        COMPILATION_ERROR, RUNTIME_ERROR, SUCCESS
    }

    private final ExecutionStatus status;
    private final String snippet;
    private final String result;
    private final List<GoalState> goalStates;
    private final List<Definition> definitions;

    public SnippetExecutionEvent(
            String snippet,
            ExecutionStatus status,
            String result,     // evaluation result or error message
            List<GoalState> goalStates,
            List<Definition> definitions) {
        this.snippet = snippet;
        this.status = status;
        this.result = result;
        this.goalStates = goalStates;
        this.definitions = definitions;
    }

    public SnippetExecutionEvent(
            String snippet,
            ExecutionStatus status,
            String result) {
        this(snippet, status, result, List.of(), List.of());
    }

    public String getSnippet() { return snippet; }
    public ExecutionStatus getStatus() { return status; }
    public String getResult() { return result == null ? "" : result; }
    public List<GoalState> getGoalStates() { return goalStates; }
    public List<Definition> getDefinitions() { return definitions; }

    public SnippetExecutionEvent addStates(Collection<GoalState> additionalStates) {
        var newStates = new ArrayList<>(goalStates);
        newStates.addAll(additionalStates);
        return new SnippetExecutionEvent(snippet, status, result, newStates, definitions);
    }

    public SnippetExecutionEvent addDefinitions(Collection<Definition> additionalDefs) {
        var newDefs = new ArrayList<>(definitions);
        newDefs.addAll(additionalDefs);
        return new SnippetExecutionEvent(snippet, status, result, goalStates, newDefs);
    }

    @Override
    public String toString() {
        return "[snippet: " + snippet + ", \n" +
                " status: " + status + " , \n" +
                " result: " + result + ", \n" +
                " goals: " + goalStates + ", \n" +
                " defs: " + definitions + "\n" +
                "]";
    }
}
