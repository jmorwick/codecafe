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
public class LessonController {

    private final Logger logger = Logger.getLogger(LessonController.class.getCanonicalName());


    // username x lesson -> current tool instance (if any)
    private Map<Tuple2<String,String>,JShellLessonTool> toolCache = new HashMap<>();

    public final long DEFAULT_TIMEOUT = 1000;

    /** returns the jshell tool associated with the given lesson and username.
     * If no such tool exists in memory, one is created and its history is loaded a database.
     * @param username
     * @param lesson
     * @return
     */
    public synchronized JShellLessonTool getTool(String username, String lesson) {
        var id = makeTuple(username, lesson);
        if(toolCache.containsKey(id))
            return toolCache.get(id);

        // TODO: load lesson-specific restrictions on tool from app config

        // TODO: attempt to load execution history from DB

        logger.info("starting new jshell session for " + id);
        var newTool = new JShellLessonTool(username, lesson, DEFAULT_TIMEOUT);
        toolCache.put(id, newTool);
        return newTool;
    }

    /** accepts code snippets from users for execution on jshell tool instances */
    @PostMapping("/lessons/{lesson}/exec")
    public void executeSnippet(@PathVariable("lesson") String lesson,
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
        logger.info("User " + name + " on lesson " + lesson + " executed: " + code);
        getTool(name, lesson).evaluateCodeSnippet(code);
    }

    @PostMapping("/lessons/{lesson}/stdin")
    public void sendDataToTool(@PathVariable("lesson") String lesson,
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
        logger.info("User " + name + " on lesson " + lesson + " sent to stdin: " + data);
        getTool(name, lesson).writeToStdin(data);
    }

    /** determins if the given lesson id is a valid, configured, lesson */
    public boolean validLessonId(String lesson) {
        return lesson.length() > 0; // TODO: check lesson definitions instead
    }
}
