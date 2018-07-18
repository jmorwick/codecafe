package net.sourcedestination.codecafe;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JShellEvalTerminal {

    private final JShell jshell;
    private final Set<Consumer<String>> out = new HashSet<>();  // TODO: eliminate and add other snippet / evaluation listeners
    private final Set<Consumer<Map<VarSnippet, String>>> varListeners = new HashSet<>();

    public JShellEvalTerminal(Consumer<String> out) {
        this.jshell = JShell.create();
        this.out.add(out);
    }

    public void updateConsumer(Consumer<String> out) {
        this.out.add(out);
    }

    public void receiveMessage(String message) {

        // TODO: log message and result

        var output = jshell.eval(message); // TODO: execute in a separate thread and tiemout if it takes too long
        jshell.sourceCodeAnalysis().sourceToSnippets(message);

        for(var s : output) {
            if (s.status() == Snippet.Status.REJECTED) {
                out.forEach(o -> o.accept("Error: " + s.exception()));
                // TODO: get error messages working appropriately
            } else {
                out.forEach(o -> o.accept("Result: " + s.value()));
                // TODO: only output return values, no void or definition stuff

                // TODO: limit updates to only when variable values change or new variables are defined
                varListeners.forEach(listener ->
                        listener.accept(jshell.variables().collect(
                                Collectors.toMap(v -> v, v -> jshell.varValue(v)))
                        ));
            }
        }
        out.forEach(o -> o.accept("\n> "));
    }

    public void stop() {
        jshell.stop();
    }

    public void attachVariableListener(Consumer<Map<VarSnippet, String>> callback) {
        varListeners.add(callback);
    }
}
