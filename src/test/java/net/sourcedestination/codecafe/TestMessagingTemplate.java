package net.sourcedestination.codecafe;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

public class TestMessagingTemplate extends SimpMessagingTemplate {

    public TestMessagingTemplate() {
        super(
                (MessageChannel)((message, timeout) -> true)
        );
    }

    public void setDefaultDestination(@Nullable String defaultDestination) {
    }

    @Nullable
    public String getDefaultDestination() {
        throw new IllegalStateException("Not Implemented");
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        throw new IllegalStateException("Not Implemented");
    }

    public MessageConverter getMessageConverter() {
        throw new IllegalStateException("Not Implemented");
    }

    public void send(Message<?> message) {
        throw new IllegalStateException("Not Implemented");
    }

    public void send(String destination, Message<?> message) {
        throw new IllegalStateException("Not Implemented");
    }

    protected void doSend(String var1, Message<?> var2) {
        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(Object payload) throws MessagingException {
        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(String destination, Object payload) throws MessagingException {
        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(String destination, Object payload, @Nullable Map<String, Object> headers) throws MessagingException {

        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(Object payload, @Nullable MessagePostProcessor postProcessor) throws MessagingException {

        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(String destination, Object payload, @Nullable MessagePostProcessor postProcessor) throws MessagingException {

        throw new IllegalStateException("Not Implemented");
    }

    public void convertAndSend(String destination, Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor) throws MessagingException {

        throw new IllegalStateException("Not Implemented");
    }

    protected Message<?> doConvert(Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor) {

        throw new IllegalStateException("Not Implemented");
    }

    @Nullable
    protected Map<String, Object> processHeadersToSend(@Nullable Map<String, Object> headers) {
        throw new IllegalStateException("Not Implemented");
    }
}
