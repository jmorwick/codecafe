package net.sourcedestination.codecafe.structure;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.persistance.DBManager;
import net.sourcedestination.funcles.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

@Controller
public class ExerciseController {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private DBManager db;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Logger logger = Logger.getLogger(ExerciseController.class.getCanonicalName());

    // exeriseId -> template name
    private Map<String, ExerciseDefinition> definitions = new HashMap<>();
    private Map<Tuple2<String,String>,JShellExerciseTool> toolCache = new HashMap<>();

    public final long DEFAULT_TIMEOUT = 100000;

    /** returns the jshell tool associated with the given exerciseId and username.
     * If no such tool exists in memory, one is created and its history is loaded a database.
     * @param username
     * @param exerciseId
     * @return
     */
    public synchronized JShellExerciseTool getTool(String username, String exerciseId) {
        var id = makeTuple(username, exerciseId);
        if(toolCache.containsKey(id))
            return toolCache.get(id);

        if(!definitions.containsKey(exerciseId)) {
            Map<String, ExerciseDefinition> exerciseBeans =
                    appContext.getBeansOfType(ExerciseDefinition.class);
            if (!exerciseBeans.containsKey(exerciseId)) {
                throw new IllegalArgumentException("No such exercise: " + exerciseId);
            }
            var def = exerciseBeans.get(exerciseId);
            definitions.put(exerciseId, def);
        }

        // TODO: attempt to load execution history from DB

        logger.info("starting new jshell session for " + id);
        var newTool = new JShellExerciseTool(username, exerciseId, db,
                DEFAULT_TIMEOUT,
                messagingTemplate,
                definitions.get(exerciseId));
        toolCache.put(id, newTool);
        return newTool;
    }

    public ExerciseDefinition getDefinition(String exerciseId) {
        return definitions.get(exerciseId);
    }

    @GetMapping("/exercises/{exerciseId}")
    public String viewExercise(Map<String, Object> model,
                               @PathVariable("exerciseId") String exerciseId,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        var username = request.getUserPrincipal().getName();
        model.put("exerciseId", exerciseId);

        return "single-exercise.html";
    }

    @GetMapping("/exercises/{exerciseId}/raw")
    public String viewRawExercise(Map<String, Object> model,
                               @PathVariable("exerciseId") String exerciseId,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        var username = request.getUserPrincipal().getName();
        var tool = getTool(username, exerciseId); // locating tool loads template def
        var def = getDefinition(exerciseId);
        if(tool == null) {
            response.sendError(403, "could not start jshell tool");
            return null;
        }

        model.put("goal", def.getGoalStructure().toStateMap());
        model.put("exerciseId", exerciseId);
        return "exercises/"+def.getTemplate()+".html";
    }

    @MessageMapping("/exercise/{exerciseId}/exec")
    public void executeSnippet(
            String code,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {

        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " executed: " + code);
        getTool(username, exerciseId).evaluateCodeSnippet(code);
    }

    @MessageMapping("/exercise/{exerciseId}/stdin")
    public void sendDataToTool(
            String data,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {

        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " sent to stdin: " + data);
        getTool(username, exerciseId).writeToStdin(data);
    }

    @MessageMapping("/exercise/{exerciseId}/reset")
    public void resetTool(
            String data,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {
        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " issued reset");
        getTool(username, exerciseId).reset();
        //db.recordReset(username, exerciseId);
    }

    /** determins if the given exercise id is a valid, configured, exercise */
    public boolean validExerciseId(String exerciseId) {
        return exerciseId.length() > 0; // TODO: check exercise definitions instead
    }
}
