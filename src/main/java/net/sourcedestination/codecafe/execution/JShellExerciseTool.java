package net.sourcedestination.codecafe.execution;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.jshell.*;
import net.sourcedestination.codecafe.persistance.DBManager;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class JShellExerciseTool {
    private final Logger logger = Logger.getLogger(JShellExerciseTool.class.getCanonicalName());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private final String username;
    private final String exerciseId;
    private JShell jshell;
    private long timeout;
    private PrintWriter toStdin;
    private final Set<Restriction> restrictions;
    private final Map<List<String>, Goal> goals;
    private final DBManager db;
    private final Gson gson;


    public JShellExerciseTool(String username, String exerciseId, DBManager db,
                              long timeout,
                              Collection<Restriction> restrictions,
                              List<Goal> goals) {
        try {
            var out = new PipedOutputStream();
            var in = new PipedInputStream(out);
            this.exerciseId = exerciseId;
            this.username = username;
            this.db = db;
            this.toStdin = new PrintWriter(out);
            this.jshell = buildJShell();
            var gb = new GsonBuilder();
            gson = gb.create();
        } catch(IOException e) {
            throw new IllegalStateException("could not initialize jshell");
        }
        this.timeout = timeout;
        this.restrictions = ImmutableSet.copyOf(restrictions);
        this.goals = new HashMap<>();
        for(var g : goals) {
            this.goals.put(g.getId(), g);
        }
        replaySavedInteractions();
    }

    private JShell buildJShell() {
        var jshell = JShell.builder()
                .out(new PrintStream(new OutputStream() {
                    @Override public void write(int b) throws IOException {
                        // TODO: buffer this
                        sendStdout(""+((char)b));
                    }
                }))
                //              .in(in)    // TODO: using this suspends the jshell -- need to figure out why
                .build();
        return jshell;
    }

    private void replaySavedInteractions() {
        if(db != null) db.retrieveHistory(username,exerciseId).forEach(code -> {
            logger.info("replaying: " + code );
            evaluateCodeSnippet(code);
        });
    }

    public synchronized void evaluateCodeSnippet(String code) {
        if(jshell.sourceCodeAnalysis().sourceToSnippets(code).stream()
                .flatMap(s -> restrictions.stream()
                        .filter(r -> r.apply(s, this))
                        .map(r -> makeTuple(s, r)))
                .map(t-> t._2.getReason(t._1, this))
                .distinct()
                .map(
                        reason ->  {
                            sendError(code,reason);

                            db.recordSnippet(username,exerciseId,code, true);
                            return reason;
                        }
                ).count() > 0) return; // quit if a restriction fires
        CompletableFuture.supplyAsync(() -> jshell.eval(code))
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenAccept(results -> { // update history listeners
                    results.forEach(s -> {
                        if (s.status() == Snippet.Status.REJECTED) {
                            sendError(code, ""+s.exception());
                            db.recordSnippet(username,exerciseId, code,true);
                            // TODO: get error messages working appropriately
                        } else {
                            sendSnippetHistory(s);
                            db.recordSnippet(username,exerciseId, code,false);
                        }
                    });
                }).thenRun(() -> {
                    sendVariables();
                    sendMethods();
                    goals.values().stream()  // for each goal
                    .forEach(this::sendGoalStatus); // update client
                }).exceptionally(e -> {
                    jshell.stop();
                    sendError(code, "Last statement went over time");
                    return null;
                });
    }

    public void sendSnippetHistory(SnippetEvent e) {
        // TODO: send JSON to client via STOMP
    }

    public void sendStdout(String message) {
        // TODO: send JSON to client via STOMP
    }

    public void sendMethods() {
        var methods = jshell.methods().collect(Collectors.toList());
        // TODO: send JSON to client via STOMP
    }

    public void sendVariables() {
        var vars = jshell.variables().collect(
                Collectors.toMap(v -> v, v -> jshell.varValue(v)));
        var json = gson.toJson(vars);
        messagingTemplate.convertAndSendToUser(username,"/exercises/"+exerciseId, json); // TODO: test
    }

    public void sendError(String offendingSnippet, String errorMessage) {
        // TODO: send JSON to client via STOMP
    }

    public void sendGoalStatus(Goal goal) {
        var m = new HashMap<String,String>();
        m.put("id", gson.toJson(goals.get(goal)));
        var results = goal.completionPercentage(this);
        m.put("completion", ""+results._1);
        m.put("message", results._2);
        messagingTemplate.convertAndSendToUser(username,"/exercises/"+exerciseId, m); // TODO: test
    }

    public synchronized JShell getShell() { return jshell; }
    public synchronized void writeToStdin(String data) {
        toStdin.print(data);
    }
    public synchronized void reset() {
        jshell.stop();
        jshell = buildJShell();
    }
}
