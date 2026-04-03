package io.github.rNetAi.rnetCore.rNetProtocol.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.RNetRequest;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Tickets;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import io.github.rNetAi.rnetCore.rNetProtocol.objWrapper.RNetProtocolHttpRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RNetGlobalRequestFilter extends OncePerRequestFilter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static RNetRequest parseProtocolRequest(HttpServletRequest httpRequest)
            throws IOException, IllegalArgumentException {

        String requestBody = httpRequest.getReader().lines().collect(Collectors.joining());

        if (requestBody.isEmpty()) {
            throw new IllegalArgumentException("Empty protocol request body");
        }

        JsonNode rootNode;
        try {
            rootNode = OBJECT_MAPPER.readTree(requestBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid RNET JSON", e);
        }

        JsonNode ticketsNode = rootNode.get(RNetProtocol.RNET_IN);
        JsonNode bodyNode = rootNode.get("body");
        JsonNode metadataNode = rootNode.get("metadata");

        if (ticketsNode == null || metadataNode == null
                || metadataNode.get("originalMethod") == null
                || metadataNode.get("contentType") == null) {
            throw new IllegalArgumentException("Invalid RNET JSON: missing required fields");
        }

        String finalBodyContent = "{}";
        if (bodyNode != null && !bodyNode.isNull()) {
            finalBodyContent = bodyNode.isTextual() ? bodyNode.asText() : OBJECT_MAPPER.writeValueAsString(bodyNode);
        }

        String originalMethod = metadataNode.get("originalMethod").asText();
        String originalContentType = metadataNode.get("contentType").asText();

        return new RNetRequest(ticketsNode, finalBodyContent, originalMethod, originalContentType);
    }

    private static RNetProtocolHttpRequestWrapper buildWrappedRequest(HttpServletRequest originalRequest, RNetRequest RNetRequest) {
        RNetProtocolHttpRequestWrapper wrappedRequest = new RNetProtocolHttpRequestWrapper(
                originalRequest,
                RNetRequest.getFinalBody(),
                RNetRequest.getMethod()
        );

        if (RNetRequest.getContentType() != null) {
            wrappedRequest.setHeader("Content-Type", RNetRequest.getContentType());
        }

        return wrappedRequest;
    }

    private static Map<Long, List<String>> parseTicketsStrict(JsonNode ticketsNode) {
        if (ticketsNode == null || !ticketsNode.isObject()) {
            throw new IllegalArgumentException("Tickets must be a JSON object");
        }

        Map<Long, List<String>> ticketsMap = new HashMap<>();
        ticketsNode.fieldNames().forEachRemaining(ticketIdStr -> {
            long ticketId;
            try {
                ticketId = Long.parseLong(ticketIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid ticket id: " + ticketIdStr);
            }

            JsonNode ticketValuesNode = ticketsNode.get(ticketIdStr);
            if (ticketValuesNode == null || ticketValuesNode.isNull() || !ticketValuesNode.isArray()) {
                throw new IllegalArgumentException("Expected non-null array for ticket id: " + ticketId);
            }

            List<String> ticketValues = new ArrayList<>();
            for (JsonNode ticketValueNode : ticketValuesNode) {
                if (!ticketValueNode.isTextual()) {
                    throw new IllegalArgumentException("Non-string value in array for ticket id: " + ticketId);
                }
                ticketValues.add(ticketValueNode.asText());
            }

            ticketsMap.put(ticketId, ticketValues);
        });

        return ticketsMap;
    }

    /** Determine if this filter should be skipped for this request */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        boolean isRNetProtocolHeader = "true".equalsIgnoreCase(request.getHeader(RNetProtocol.RNET_HEADER));
        boolean routeExists = RoutesContext.isPathPresent(requestPath);
        return !(isRNetProtocolHeader && routeExists);
    }

    /** Core filter logic */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        RNetRequest rNetRequest;
        try {
            rNetRequest = parseProtocolRequest(request);

            Tickets tickets = new Tickets(parseTicketsStrict(rNetRequest.getTickets()));
            request.setAttribute(RNetProtocol.RNET_IN, tickets);

            Usage usage = new Usage(tickets.getSize());
            request.setAttribute(RNetProtocol.RNET_OUT, usage);

        } catch (IllegalArgumentException e) {
            writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), new HashMap<>());
            return;
        }

        RNetProtocolHttpRequestWrapper wrappedRequest = buildWrappedRequest(request, rNetRequest);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, cachedResponse);
        } catch (Exception e) {
            Usage usage = (Usage) request.getAttribute(RNetProtocol.RNET_OUT);
            Object usageData = usage != null ? usage.getUsages() : new HashMap<>();
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), usageData);
            return;
        }

        cachedResponse.copyBodyToResponse();
    }

    private static void writeErrorResponse(HttpServletResponse response,
                                           int status,
                                           String message,
                                           Object usageData) throws IOException {
        response.reset();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectNode errorNode = OBJECT_MAPPER.createObjectNode();
        errorNode.put("error", message);
        errorNode.set(RNetProtocol.RNET_OUT, OBJECT_MAPPER.valueToTree(usageData));

        String body = OBJECT_MAPPER.writeValueAsString(errorNode);
        response.setContentLength(body.getBytes(StandardCharsets.UTF_8).length);

        PrintWriter writer = response.getWriter();
        writer.write(body);
        writer.flush();
    }
}
