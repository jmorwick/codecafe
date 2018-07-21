package net.sourcedestination.codecafe;

import net.sourcedestination.funcles.consumer.Consumer2;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.logging.Logger;

class ExerciseWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(ExerciseWebsocketHandler.class.getCanonicalName());

    private final Consumer2<JShellExerciseTool,WebSocketSession> connectAction;
    private final String name;
    private final ExerciseController exercises;

    public ExerciseWebsocketHandler(ExerciseController exercises, String name,
                                    Consumer2<JShellExerciseTool,WebSocketSession> connectAction) {
        this.connectAction = connectAction;
        this.exercises = exercises;
        this.name = name;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // get exercise ID from end of URL
        var path = session.getUri().getPath().split("/");
        var exerciseId = path[path.length-2];
        if(!exercises.validExerciseId(exerciseId)) {
            // bad exerciseId ID
            session.close(CloseStatus.PROTOCOL_ERROR);
            return;
        }

        if(session.getPrincipal() == null ) {
            // ERROR: user not logged in
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        var username = session.getPrincipal().getName();

        logger.info("Connection " + session.getId() +
                " to " + name + " for exerciseId " + exerciseId + " by " + username);

        actOnConnect(exercises.getTool(username, exerciseId), session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }

    @Override
    public boolean supportsPartialMessages() { return false; }

    /** acts when a connection is made.
     * If only a simple action is needed, this will delegate to function provided
     * to constructor. Otherwise, this class should be extended with this method being
     * overridden and a no-op or null value passed in to the parent constructor.
     *
     * @param tool
     * @param session
     */
    public void actOnConnect(JShellExerciseTool tool, WebSocketSession session) {
        connectAction.accept(tool, session);
    }
}
