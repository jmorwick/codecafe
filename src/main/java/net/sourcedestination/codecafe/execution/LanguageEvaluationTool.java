package net.sourcedestination.codecafe.execution;

public interface SnippetEvaluator<A> {
    public A evaluateCodeSnippet(String snippet);
}
