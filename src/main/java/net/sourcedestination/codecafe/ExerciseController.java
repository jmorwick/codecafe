package net.sourcedestination.codecafe;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import net.sourcedestination.funcles.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private final Logger logger = Logger.getLogger(ExerciseController.class.getCanonicalName());

    // exeriseId -> template name
    private Map<String,ExerciseDefinition> definitions = new HashMap<>();
    // exerciseId -> restrictions
    private Multimap<String,Restriction> restrictions = HashMultimap.create();
    // exerciseId -> goals
    private Multimap<String,Goal> goals = HashMultimap.create();
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

        if(!definitions.containsKey(exerciseId)) {
            Map<String, ExerciseDefinition> exerciseBeans =
                    appContext.getBeansOfType(ExerciseDefinition.class);
            if (!exerciseBeans.containsKey(exerciseId)) {
                throw new IllegalArgumentException("No such exercise: " + exerciseId);
            }
            var def = exerciseBeans.get(exerciseId);
            definitions.put(exerciseId, def);
            def.getRestrictions().forEach(r -> restrictions.put(exerciseId, r));
            def.getGoals().forEach(g -> goals.put(exerciseId, g));
        }

        // TODO: attempt to load execution history from DB

        logger.info("starting new jshell session for " + id);
        var newTool = new JShellExerciseTool(username, exerciseId, db,
                DEFAULT_TIMEOUT,
                restrictions.get(exerciseId),
                goals.get(exerciseId));
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

        var goalDescriptions = def.getGoals().map(Goal::getDescription).collect(Collectors.toList());
        model.put("goals", goalDescriptions);
        model.put("exerciseId", exerciseId);

        return "exercises/"+def.getTemplate()+".html";
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


    @PostMapping("/exercises/{exerciseId}/reset")
    public void resetTool(@PathVariable("exerciseId") String exerciseId,
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
        var username = request.getUserPrincipal().getName();
        logger.info("User " + username + " on exercise " + exerciseId + " issued reset");
        getTool(username, exerciseId).reset();
        db.recordReset(username, exerciseId);
    }

    /** determins if the given exercise id is a valid, configured, exercise */
    public boolean validExerciseId(String exerciseId) {
        return exerciseId.length() > 0; // TODO: check exercise definitions instead
    }
}
