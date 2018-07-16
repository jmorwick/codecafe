package net.sourcedestination.codecafe;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;

import java.util.function.Consumer;

public class JShellEvalTerminal {

    private final JShell jshell;
    private final Consumer<String> out;

    public JShellEvalTerminal(Consumer<String> out) {
        this.jshell = JShell.create();
        this.out = out;
        out.accept("\n> "); // TODO: perhaps drop the idea of an interactive shell appearance
    }

    public void receiveMessage(String message) {
        var output = jshell.eval(message); // TODO: execute in a separate thread and tiemout if it takes too long

        for(var s : output) {
            if (s.status() == Snippet.Status.REJECTED) {
                out.accept("Error: " + s.exception());
                // TODO: get error messages working appropriately
            } else {
                out.accept("Result: " + s.value());
                // TODO: only output return values, no void or definition stuff
            }
        }
        out.accept("\n> ");
    }
}
