package io.github.rNetAi.rnetCore.rNetProtocol.entity;

import com.fasterxml.jackson.databind.JsonNode;

public class RNetRequest {
    private final JsonNode tickets;
    private final String finalBody;
    private final String method;
    private final String contentType;

    public RNetRequest(JsonNode tickets, String finalBody, String method, String contentType) {
        this.tickets = tickets;
        this.finalBody = finalBody;
        this.method = method;
        this.contentType = contentType;
    }

    public JsonNode getTickets() { return tickets; }
    public String getFinalBody() { return finalBody; }
    public String getMethod() { return method; }
    public String getContentType() { return contentType; }
}