package net.sourcedestination.codecafe;

import net.sourcedestination.funcles.tuple.Tuple2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

@Controller
public class ExerciseController {

    private final Logger logger = Logger.getLogger(ExerciseController.class.getCanonicalName());


    // username x exerciseId -> current tool instance (if any)
    private Map<Tuple2<String,String>,JShellExerciseTool> toolCache = new HashMap<>();

    public final long DEFAULT_TIMEOUT = 1000;

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

        // TODO: load exercise-specific restrictions on tool from app config

        // TODO: attempt to load execution history from DB

        logger.info("starting new jshell session for " + id);
        var newTool = new JShellExerciseTool(username, exerciseId, DEFAULT_TIMEOUT);
        toolCache.put(id, newTool);
        return newTool;
    }

    /** accepts code snippets from users for execution on jshell tool instances */
    @PostMapping("/exercises/{exerciseId}/exec")
    public void executeSnippet(@PathVariable("exerciseId") String exerciseId,
                               @RequestParam("code") String code,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if(request.getUserPrincipal() == null ) {
            // ERROR: user not logged in
            try {
                response.sendError(403, "user must be logged in to use jshell instances");
            } catch(IOException e) {
                // TODO: log error
            }
        }
        var name = request.getUserPrincipal().getName();
        logger.info("User " + name + " on exercise " + exerciseId + " executed: " + code);
        getTool(name, exerciseId).evaluateCodeSnippet(code);
    }

    @PostMapping("/exercises/{exerciseId}/stdin")
    public void sendDataToTool(@PathVariable("exerciseId") String exerciseId,
                               @RequestParam("data") String data,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if(request.getUserPrincipal() == null ) {
            // ERROR: user not logged in
            try {
                response.sendError(403, "user must be logged in to use jshell instances");
            } catch(IOException e) {
                // TODO: log error
            }
        }
        var name = request.getUserPrincipal().getName();
        logger.info("User " + name + " on exercise " + exerciseId + " sent to stdin: " + data);
        getTool(name, exerciseId).writeToStdin(data);
    }

    /** determins if the given exercise id is a valid, configured, exercise */
    public boolean validExerciseId(String exerciseId) {
        return exerciseId.length() > 0; // TODO: check exercise definitions instead
    }
}
