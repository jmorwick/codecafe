package net.sourcedestination.codecafe;

import jdk.jshell.*;
import net.sourcedestination.funcles.consumer.Consumer2;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JShellLessonTool {
    private final Logger logger = Logger.getLogger(JShellLessonTool.class.getCanonicalName());

    private JShell jshell;
    private long timeout;
    private PrintWriter toStdin;
    private List<SnippetEvent> history = new ArrayList<>();

    private final Set<Consumer<SnippetEvent>> historyListeners = new HashSet<>();
    private final Set<Consumer<Map<VarSnippet, String>>> varListeners = new HashSet<>();
    private final Set<Consumer2<String,String>> errorListeners = new HashSet<>();
    private final Set<Consumer<List<MethodSnippet>>> methodListeners = new HashSet<>();
    private final Set<Consumer<String>> stdoutListeners = new HashSet<>();
    // TODO: add test listeners

    public JShellLessonTool(String username, String lesson, long timeout) {
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
    }

    public void evaluateCodeSnippet(String code) {
        // TODO: check for and replace an existing method if its being redefined
        // TODO: analyze snippet and check for errors before executing
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
}
