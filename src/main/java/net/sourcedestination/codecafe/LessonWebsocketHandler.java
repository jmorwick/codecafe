package net.sourcedestination.codecafe;

import net.sourcedestination.funcles.consumer.Consumer2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.logging.Logger;

class LessonWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(LessonWebsocketHandler.class.getCanonicalName());

    private final Consumer2<JShellLessonTool,WebSocketSession> connectAction;
    private final String name;
    private final LessonController lessons;

    public LessonWebsocketHandler(LessonController lessons, String name,
                                  Consumer2<JShellLessonTool,WebSocketSession> connectAction) {
        this.connectAction = connectAction;
        this.lessons = lessons;
        this.name = name;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // get lesson ID from end of URL
        var path = session.getUri().getPath().split("/");
        var lesson = path[path.length-2];
        if(!lessons.validLessonId(lesson)) {
            // bad lesson ID
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
                " to " + name + " for lesson " + lesson + " by " + username);

        actOnConnect(lessons.getTool(username, lesson), session);
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
    public void actOnConnect(JShellLessonTool tool, WebSocketSession session) {
        connectAction.accept(tool, session);
    }
}
