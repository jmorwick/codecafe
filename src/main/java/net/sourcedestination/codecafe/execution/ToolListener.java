package net.sourcedestination.codecafe.execution;

import net.sourcedestination.codecafe.persistance.SnippetExecutionEvent;

import java.util.function.Consumer;

public interface ToolListener extends Consumer<SnippetExecutionEvent> {

}
