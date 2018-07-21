package net.sourcedestination.codecafe;

import com.google.common.collect.ImmutableSet;
import jdk.jshell.*;
import net.sourcedestination.funcles.consumer.Consumer2;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JShellExerciseTool {
    private final Logger logger = Logger.getLogger(JShellExerciseTool.class.getCanonicalName());

    private JShell jshell;
    private long timeout;
    private PrintWriter toStdin;
    private List<SnippetEvent> history = new ArrayList<>();

    private final Set<Consumer<SnippetEvent>> historyListeners = new HashSet<>();
    private final Set<Consumer<Map<VarSnippet, String>>> varListeners = new HashSet<>();
    private final Set<Consumer2<String,String>> errorListeners = new HashSet<>();
    private final Set<Consumer<List<MethodSnippet>>> methodListeners = new HashSet<>();
    private final Set<Consumer<String>> stdoutListeners = new HashSet<>();
    private final Set<Consumer2<String,Double>> goalListeners = new HashSet<>();
    private final Set<Restriction> restrictions;
    private final Set<Goal> goals;

    public JShellExerciseTool(String username, String exerciseId, long timeout,
                              List<Restriction> restrictions,
                              List<Goal> goals) {
        try {
            var out = new PipedOutputStream();
            var in = new PipedInputStream(out);
            this.toStdin = new PrintWriter(out);
            this.jshell = JShell.builder()
                    .out(new PrintStream(new OutputStream() {
                        @Override public void write(int b) throws IOException {
                            // TODO: buffer this
                            stdoutListeners.forEach(out -> out.accept(""+((char)b)));
                        }
                    }))
      //              .in(in)    // TODO: using this suspends the jshell -- need to figure out why
                    .build();
        } catch(IOException e) {
            throw new IllegalStateException("could not initialize jshell");
        }
        this.timeout = timeout;
        this.restrictions = ImmutableSet.copyOf(restrictions);
        this.goals = ImmutableSet.copyOf(goals);
    }

    public void evaluateCodeSnippet(String code) {
        if(jshell.eval(code).stream()
            .flatMap(s -> restrictions.stream().filter(r -> r.apply(s, this))
                .map(Restriction::getReason))
            .distinct()
            .map(
                   reason ->  {
                       errorListeners.forEach(o -> o.accept(code,reason));
                       return reason;
                   }
            ).count() > 0) return; // quit if a restriction fires
        CompletableFuture.supplyAsync(() -> jshell.eval(code))
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenAccept(results -> { // update history listeners
                    results.forEach(s -> {
                        if (s.status() == Snippet.Status.REJECTED) {
                            errorListeners.forEach(o -> o.accept(code, ""+s.exception()));
                            // TODO: get error messages working appropriately
                        } else {
                            historyListeners.forEach(o -> o.accept(s));
                        }
                    });
                })
                .thenRun(() -> {  // update var listeners
                    varListeners.forEach(listener ->
                            listener.accept(jshell.variables().collect(
                                    Collectors.toMap(v -> v, v -> jshell.varValue(v)))
                            ));
                })
                .thenRun(() -> {  // update method listeners
                    methodListeners.forEach(listener ->
                            listener.accept(jshell.methods().collect(Collectors.toList())));
                })
                .exceptionally(e -> {
                    jshell.stop();
                    errorListeners.forEach(o -> o.accept(code, "Last statement went over time"));
                    return null;
                });
        goals.forEach(goal ->
                goalListeners.forEach(o ->
                        o.accept(goal.getDescription(), goal.completionPercentage(this))));
    }

    public synchronized void writeToStdin(String data) {
        toStdin.print(data);
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
    public void attachGoalsListener(Consumer2<String,Double> callback) {
        goalListeners.add(callback);
    }
}
