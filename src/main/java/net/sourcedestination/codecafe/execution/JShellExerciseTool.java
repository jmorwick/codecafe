package net.sourcedestination.codecafe.execution;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import jdk.jshell.*;
import net.sourcedestination.codecafe.persistance.DBManager;
import net.sourcedestination.codecafe.structure.goals.Goal;
import net.sourcedestination.codecafe.structure.goals.GoalStatus;
import net.sourcedestination.codecafe.structure.restrictions.Restriction;
import net.sourcedestination.funcles.consumer.Consumer2;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
    private List<SnippetEvent> history = new ArrayList<>();

    private final Set<Consumer<SnippetEvent>> historyListeners = new HashSet<>();
    private final Set<Consumer<Map<VarSnippet, String>>> varListeners = new HashSet<>();
    private final Set<Consumer2<String,String>> errorListeners = new HashSet<>();
    private final Set<Consumer<List<MethodSnippet>>> methodListeners = new HashSet<>();
    private final Set<Consumer<String>> stdoutListeners = new HashSet<>();
    private final Set<Consumer<GoalStatus>> goalListeners = new HashSet<>();
    private final Set<Restriction> restrictions;
    private final BiMap<Goal, List<String>> goals;
    private final DBManager db;

    public JShellExerciseTool(String username, String exerciseId, DBManager db,
                              long timeout,
                              Collection<Restriction> restrictions,
                              Tuple2<Goal, List<String>> ... goals) {
        try {
            var out = new PipedOutputStream();
            var in = new PipedInputStream(out);
            this.exerciseId = exerciseId;
            this.username = username;
            this.db = db;
            this.toStdin = new PrintWriter(out);
            this.jshell = buildJShell();

        } catch(IOException e) {
            throw new IllegalStateException("could not initialize jshell");
        }
        this.timeout = timeout;
        this.restrictions = ImmutableSet.copyOf(restrictions);
        this.goals = HashBiMap.create();
        Arrays.stream(goals) // add each goal, along with its path and ID, to the goals
                .forEach(((Consumer2<Goal,List<String>>)this.goals::put)::accept);
    }

    private JShell buildJShell() {
        var jshell = JShell.builder()
                .out(new PrintStream(new OutputStream() {
                    @Override public void write(int b) throws IOException {
                        // TODO: buffer this
                        stdoutListeners.forEach(out -> out.accept(""+((char)b)));
                    }
                }))
                //              .in(in)    // TODO: using this suspends the jshell -- need to figure out why
                .build();
        if(db != null) db.retrieveHistory(username,exerciseId).forEach(code -> {
            logger.info("replaying: " + code);
            evaluateCodeSnippet(code);
        });
        return jshell;
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
                            errorListeners.forEach(o -> o.accept(code,reason));

                            db.recordSnippet(username,exerciseId,code, true);
                            return reason;
                        }
                ).count() > 0) return; // quit if a restriction fires
        CompletableFuture.supplyAsync(() -> jshell.eval(code))
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenAccept(results -> { // update history listeners
                    results.forEach(s -> {
                        if (s.status() == Snippet.Status.REJECTED) {
                            errorListeners.forEach(o -> o.accept(code, ""+s.exception()));
                            db.recordSnippet(username,exerciseId, code,true);
                            // TODO: get error messages working appropriately
                        } else {
                            historyListeners.forEach(o -> o.accept(s));
                            db.recordSnippet(username,exerciseId, code,false);
                        }
                    });
                }).thenRun(() -> {  // update var listeners
                    varListeners.forEach(listener ->
                            listener.accept(jshell.variables().collect(
                                    Collectors.toMap(v -> v, v -> jshell.varValue(v)))
                            ));
                }).thenRun(() -> {  // update method listeners
                    methodListeners.forEach(listener ->
                            listener.accept(jshell.methods().collect(Collectors.toList())));
                }).thenRun(() -> {
                    goals.keySet().stream()  // for each goal
                    .map(g -> g.getStatus(this)) // get it's status
                    .forEach(status -> // then update each goal listener with that status
                            goalListeners.forEach(gl -> gl.accept(status))
                    );
                }).exceptionally(e -> {
                    jshell.stop();
                    errorListeners.forEach(o -> o.accept(code, "Last statement went over time"));
                    return null;
                });
    }

    /** "path" indicating the nesting of this goal within a tree of goals for an exercise.
     * The final element of this list will be unique and will identify the goal relative to
     * this exercise. */
    public List<String> getGoalId(Goal g) {
        return goals.get(g);
    }

    public synchronized JShell getShell() { return jshell; }
    public synchronized void writeToStdin(String data) {
        toStdin.print(data);
    }
    public synchronized void reset() {
        jshell.stop();
        jshell = buildJShell();
    }

    public void attachHistoryListener(Consumer<SnippetEvent> callback) {
        historyListeners.add(callback);
    }
    public void attachVariableListener(Consumer<Map<VarSnippet, String>> callback) {
        varListeners.add(callback);
    }
    public void attachErrorListener(Consumer2<String,String> callback) {
        errorListeners.add(callback);
    }
    public void attachMethodListener(Consumer<List<MethodSnippet>> callback) {
        methodListeners.add(callback);
    }
    public void attachStdoutListener(Consumer<String> callback) {
        stdoutListeners.add(callback);
    }
    public void attachGoalsListener(Consumer<GoalStatus> callback) {
        goalListeners.add(callback);
    }
}
