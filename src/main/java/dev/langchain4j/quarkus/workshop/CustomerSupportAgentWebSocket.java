package dev.langchain4j.quarkus.workshop;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/timereport-agent")
public class CustomerSupportAgentWebSocket {

    private final CustomerSupportAgent customerSupportAgent;

    public CustomerSupportAgentWebSocket(CustomerSupportAgent customerSupportAgent) {
        this.customerSupportAgent = customerSupportAgent;
    }

    @OnOpen
    public String onOpen() {
        return "Welcome to RegisterTimely! How can I help you today?";
    }

    // --8<-- [start:tools]
    @OnTextMessage
    public String onTextMessage(String message) {
        return customerSupportAgent.chat(message);
    }
    // --8<-- [end:tools]
}
