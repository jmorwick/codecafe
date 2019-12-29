package net.sourcedestination.codecafe.structure;

import com.google.gson.Gson;
import net.sourcedestination.codecafe.execution.JShellJavaTool;
import net.sourcedestination.codecafe.execution.LanguageExecutionTool;
import net.sourcedestination.codecafe.execution.ToolListener;
import net.sourcedestination.codecafe.networking.JShellExerciseConnection;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
    private Map<Tuple2<String,String>, JShellJavaTool> toolCache = new ConcurrentHashMap<>();
    private Map<JShellJavaTool, List<? extends ToolListener>> listeners = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public final long DEFAULT_TIMEOUT = 100000;

    /** returns the jshell tool associated with the given exerciseId and username.
     * If no such tool exists in memory, one is created and its history is loaded a database.
     * @param username
     * @param exerciseId
     * @return
     */
    public synchronized JShellJavaTool getTool(String username, String exerciseId) {
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
        var exercise = definitions.get(exerciseId);
        var newTool = new JShellJavaTool(DEFAULT_TIMEOUT);
        exercise.initializeTool((LanguageExecutionTool)newTool);
        toolCache.put(id, newTool);
        listeners.put(newTool, List.of(
                new JShellExerciseConnection(username, messagingTemplate, exercise),
                db.getDBUpdater(username, exercise)
        ));
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
        model.put("description", def.getDescription());
        model.put("exerciseId", exerciseId);
        return "exercises/"+def.getTemplate()+".html";
    }


    @GetMapping("/exercises/{exerciseId}/history")
    @ResponseBody
    public String viewExerciseHistory(
                                  @PathVariable("exerciseId") String exerciseId,
                                  Principal user) throws IOException {
        var history = db.retrieveHistory(user.getName(), exerciseId);
        return "{\n" +
                "  \"data\": "
                +gson.toJson(history) +
                "}";
    }


    @MessageMapping("/exercise/{exerciseId}/exec")
    @ResponseBody
    public String executeSnippet(
            String code,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {
        var exercise = definitions.get(exerciseId);
        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " executed: " + code);
        var tool = getTool(username, exerciseId);
        var results = tool.executeUserCode(code, exercise, tool, listeners.get(tool));
        var result = gson.toJson(results);
        return result;
    }
/*
    @MessageMapping("/exercise/{exerciseId}/stdin")
    public void sendDataToTool(
            String data,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {

        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " sent to stdin: " + data);
        getTool(username, exerciseId).writeToStdin(data);
    }

 */

    @MessageMapping("/exercise/{exerciseId}/reset")
    public void resetTool(
            String data,
            @DestinationVariable("exerciseId") String exerciseId,
            Principal user) {
        var username = user.getName();
        logger.info("User " + username + " on exercise " + exerciseId + " issued reset");

        var id = makeTuple(username, exerciseId);
        var tool = toolCache.get(id);
        tool.stop();
        toolCache.remove(id);
        getTool(username, exerciseId); // regenerate tool
        //db.recordReset(username, exerciseId);
    }

    /** determins if the given exercise id is a valid, configured, exercise */
    public boolean validExerciseId(String exerciseId) {
        return exerciseId.length() > 0; // TODO: check exercise definitions instead
    }
}
