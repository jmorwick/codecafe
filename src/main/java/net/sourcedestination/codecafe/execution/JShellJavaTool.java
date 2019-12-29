package net.sourcedestination.codecafe.execution;

import com.google.common.collect.Sets;
import jdk.jshell.*;
import net.sourcedestination.codecafe.persistance.Definition;
import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JShellJavaTool implements LanguageEvaluationTool<Snippet>, LanguageExecutionTool {
    private final Logger logger = Logger.getLogger(JShellJavaTool.class.getCanonicalName());

    private JShell jshell;
    private long timeout;
    private final ExecutorService executor;
    private Set<Definition> currentDefinitions = null;

    private JShell buildJShell() {
        var jshell = JShell.builder().build();
        return jshell;
    }

    public JShellJavaTool(long timeout) {
        this.timeout = timeout;
        jshell = this.buildJShell();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public List<Snippet> evaluateCode(String source) {
        return jshell.sourceCodeAnalysis().sourceToSnippets(source);
    }

    private Set<Definition> getAllCurrentDefinitions() {
        var currentDefinitions = new HashSet<Definition>();
        jshell.variables().forEach(
                varSnippet ->
                        currentDefinitions.add(
                                new Definition(Definition.DefinitionType.VARIABLE,
                                        varSnippet.typeName(),
                                        varSnippet.id(),
                                        jshell.varValue(varSnippet)))

        );
        jshell.methods().forEach(
                methodSnippet ->
                        currentDefinitions.add(
                                new Definition(Definition.DefinitionType.FUNCTION,
                                        methodSnippet.signature(),
                                        methodSnippet.id(),
                                        methodSnippet.source()))
        );
        return currentDefinitions;
    }

    @Override
    public void beginCapturingDefinitions() {
        if(currentDefinitions != null)
            throw new IllegalStateException("definitions already being captured");
        currentDefinitions = getAllCurrentDefinitions();
    }

    @Override
    public synchronized List<SnippetExecutionEvent> executeRawCode(String code) {
        var future = executor.submit(
                () -> jshell.eval(code)
        );
        List<SnippetEvent> results = null;
        try {
            results = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch(TimeoutException e) {
            return List.of(new SnippetExecutionEvent(code,
                    SnippetExecutionEvent.ExecutionStatus.RUNTIME_ERROR, "over time"));
        } catch(Exception e) {  // unexpected error
            return List.of(new SnippetExecutionEvent(code,
                    SnippetExecutionEvent.ExecutionStatus.RUNTIME_ERROR, "internal error"));
        }

        if(results.size() > 0)
            return results.stream()
                .map(se -> new SnippetExecutionEvent(code, SnippetExecutionEvent.ExecutionStatus.SUCCESS, se.value()))
                .collect(Collectors.toList());
        else
            return List.of(new SnippetExecutionEvent(code, SnippetExecutionEvent.ExecutionStatus.SUCCESS, ""));

    }

    @Override
    public List<Definition> getCapturedDefinitions() {
        if(currentDefinitions == null)
            throw new IllegalStateException("Definitions not currently being captured");
        var newDefinitions = Sets.difference(currentDefinitions, getAllCurrentDefinitions());
        currentDefinitions = null;
        return new ArrayList<>(newDefinitions);
    }

    public synchronized void reset() {
        jshell.stop();
        jshell = buildJShell();
    }
    public synchronized void stop() {
        jshell.stop();
    }
}
