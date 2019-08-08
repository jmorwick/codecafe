package net.sourcedestination.codecafe.execution;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.jshell.*;
import net.sourcedestination.codecafe.persistance.DBManager;
import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class JShellExerciseTool {
    private final Logger logger = Logger.getLogger(JShellExerciseTool.class.getCanonicalName());

    private final String username;
    private final String exerciseId;
    private JShell jshell;
    private long timeout;
    private PrintWriter toStdin;
    private final Set<Restriction> restrictions;
    private final Map<String, Goal> goals;
    private final DBManager db;
    private final Gson gson;
    private final SimpMessagingTemplate messagingTemplate;
    private ExerciseDefinition exercise;

    public JShellExerciseTool(String username, String exerciseId, DBManager db,
                              long timeout,
                              SimpMessagingTemplate messagingTemplate,
                              ExerciseDefinition exercise) {
        this(username, exerciseId, db, timeout, messagingTemplate,
                exercise.getRestrictions().collect(Collectors.toList()),
                exercise.getGoalStructure().getLeafGoals().collect(Collectors.toList()));
        exercise.initializeTool(this);
        this.exercise = exercise;
    }


    public JShellExerciseTool(String username, String exerciseId, DBManager db,
                              long timeout,
                              SimpMessagingTemplate messagingTemplate,
                              Collection<Restriction> restrictions,
                              List<Goal> goals) {
        try {
            var out = new PipedOutputStream();
            var in = new PipedInputStream(out);
            this.exerciseId = exerciseId;
            this.username = username;
            this.messagingTemplate = messagingTemplate;
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
        //if(db != null) db.retrieveHistory(username,exerciseId).forEach(code -> {
            //logger.info("replaying: " + code );
            //evaluateCodeSnippet(code);  // TODO: fix
        //});
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
        directlyExecuteCodeSnippet(exercise.preprocessSnippet(code), code);

    }

    public void directlyExecuteCodeSnippet(String code, String originalCode) {
        CompletableFuture.supplyAsync(() -> jshell.eval(code))
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenAccept(results -> { // update history listeners
                    results.forEach(s -> {
                        if (s.status() == Snippet.Status.REJECTED) {
                            sendError(originalCode, ""+s.exception());
                            db.recordSnippet(username,exerciseId, originalCode,true);
                            // TODO: get error messages working appropriately
                        } else {
                            sendSnippetHistory(s);
                            db.recordSnippet(username,exerciseId, originalCode,false);
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
        if(e.value() != null) {
            messagingTemplate.convertAndSendToUser(username,
                    "/queue/exercises/"+exerciseId+"/result",
                    e.value());
        }
    }

    public void sendStdout(String message) {
        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exerciseId+"/stdout",
                message);
    }

    public void sendMethods() {
        var methods = jshell.methods()
                .map(method -> List.of(method.name(), method.signature(), method.source()))
                .collect(Collectors.toList());
        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exerciseId+"/methods",
                methods);
    }

    public void sendVariables() {
        var vars = jshell.variables().collect(
                Collectors.toMap(v -> v.name(), v -> jshell.varValue(v)));
        var json = gson.toJson(vars);
        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exerciseId+"/variables",
                json);
        // TODO: send types also
    }

    public void sendError(String offendingSnippet, String errorMessage) {
        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exerciseId+"/error",
                errorMessage);
    }

    public void sendGoalStatus(Goal goal) {
        var m = new HashMap<String,String>();
        var results = goal.completionPercentage(this);
        m.put("completion", ""+results._1);
        m.put("message", results._2);
        messagingTemplate.convertAndSendToUser(username,
                "/queue/exercises/"+exerciseId+"/goals/"+goal.getId(),
                m);
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
