package net.sourcedestination.codecafe.execution;
import net.sourcedestination.codecafe.persistance.Definition;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourcedestination.codecafe.persistance.SnippetExecutionEvent.ExecutionStatus.*;

public interface LanguageExecutionTool {
    /** executes code snippet without any transformations, restrictions, goal analysis. */
    public List<SnippetExecutionEvent> executeRawCode(String snippet);

    /** called when user code is being evaluated. */
    public void beginCapturingDefinitions();

    /** ceases definition capture and returns all new definitions and state updates since the start of the capture */
    public List<Definition> getCapturedDefinitions();


    /** applies the transformation, evaluates code restrictions, executes the snippet, and evaluates goals.*/
    public default List<SnippetExecutionEvent> executeUserCode(String code,
                                                         ExerciseDefinition exercise,
                                                         LanguageEvaluationTool evalTool,
                                                         Collection<? extends ToolListener> listeners) {

        List<SnippetExecutionEvent> events = null;
        synchronized (this) {
            var results = StreamEx.of(exercise.getEvaluationGoals())
                    .map(goal -> {
                        return goal.evaluateGoal(code, evalTool);
                    }).takeWhileInclusive(state -> !state.wasFatal())
                    .collect(Collectors.toList());

            if (results.size() > 0 && results.get(results.size() - 1).wasFatal()) {
                // snippet didn't pass through the evaluation goals -- don't execute it
                events = List.of(new SnippetExecutionEvent(code,
                        COMPILATION_ERROR,
                        results.get(results.size() - 1).getReason(),
                        results, List.of()));
            }

            if (events == null) {
                // execute user code and save new definitions
                var transformedSnippet = exercise.transformUserAttempt(code);
                beginCapturingDefinitions();
                var finalRes = executeRawCode(transformedSnippet);
                var newDefs = getCapturedDefinitions();

                if (finalRes.stream().filter(e -> e.getStatus() != SUCCESS).findAny().isPresent()) {
                    // user code failed, don't test it
                    events = finalRes.stream()
                            .map(gs -> gs.addStates(results).addDefinitions(newDefs))
                            .collect(Collectors.toList());
                }

                if (events == null) {
                    // run tests on user code and add in the results
                    StreamEx.of(exercise.getExecutionGoals())
                            .map(goal -> goal.evaluateGoal(this))
                            .takeWhileInclusive(state -> !state.wasFatal())
                            .forEach(results::add);

                    events = finalRes.stream()
                            .map(gs -> gs.addStates(results).addDefinitions(newDefs))
                            .collect(Collectors.toList());
                }
            }
        }

        for(var listener : listeners) {
            events.stream().forEach(listener::accept);
        }

        return events;
    }
}
