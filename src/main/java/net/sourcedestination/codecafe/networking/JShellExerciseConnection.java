package net.sourcedestination.codecafe.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sourcedestination.codecafe.execution.ToolListener;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.structure.goals.GoalState;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.logging.Logger;

public class JShellExerciseConnection implements ToolListener {
    private final ExerciseDefinition exercise;
    private final String username;
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson;

    private final Logger logger = Logger.getLogger(JShellExerciseConnection.class.getCanonicalName());

    public JShellExerciseConnection(String username,
                                    SimpMessagingTemplate messagingTemplate,
                                    ExerciseDefinition exercise) {
        this.username = username;
        this.exercise = exercise;
        var gb = new GsonBuilder();
        this.messagingTemplate = messagingTemplate;
        gson = gb.create();
    }


    @Override
    public void accept(SnippetExecutionEvent event) {
        event.getDefinitions().forEach(def -> {
            messagingTemplate.convertAndSendToUser(username,
                    "/queue/exercises/"+exercise.getId()+"/definition",
                    gson.toJson(def.toPropertyMap()));
        });
        event.getGoalStates().forEach(gs -> {
            var path = "/queue/exercises/"+exercise.getId()+"/goals";
            var content = gson.toJson(gs.toPropertyMap());
            messagingTemplate.convertAndSendToUser(username, path, content);
        });

        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exercise.getId()+"/result",
                gson.toJson(Map.of(
                        "status", event.getStatus(),
                        "snippet", event.getSnippet(),
                        "message", event.getResult() ,
                        "completion", event.getGoalStates().stream()
                                .mapToDouble(GoalState::getProgress).average()
                                .getAsDouble()
                )));
    }
}
