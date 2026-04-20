package io.github.rNetAi.rnetCore.rNetProtocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rNetAi.rnetCore.context.ResourceContext;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.entity.ModelInfo;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Tickets;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import io.github.rNetAi.rnetCore.rNetProtocol.exception.RNetException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class RNetResource {

    /**
     * Sends the resource request to the AI server and returns the response data.
     * Tracks usage whether the call succeeds or fails.
     */
    @SuppressWarnings("unchecked")
    public final Map<String, Object> call(
            @Nonnull HttpServletRequest req,
            @Nonnull RestTemplate restTemplate,
            Map<String, Object> resourceRequestBody
    ) throws RNetException {

        if (!RoutesContext.isPathPresent(req.getServletPath())) {
            throw new RNetException("Path is not registered for AI call");
        }

        Tickets tickets = (Tickets) req.getAttribute(RNetProtocol.RNET_IN);
        if (tickets == null) {
            throw new RNetException("Tickets are missing in request attributes");
        }

        Usage usage = (Usage) req.getAttribute(RNetProtocol.RNET_OUT);
        if (usage == null) {
            throw new RNetException("Usage attribute is missing in request");
        }

        Class<? extends RNetResource> resourceProcessorClass =
                (Class<? extends RNetResource>) ClassUtils.getUserClass(this);
        ModelInfo info = ResourceContext.getClassMapping(resourceProcessorClass);
        if (info == null) {
            throw new RNetException("ResourceInfo not found for class: " + resourceProcessorClass.getName());
        }

        List<String> ticketUrls = tickets.getResourceTicket(info.getId());
        if (ticketUrls == null) {
            throw new RNetException("No tickets found for Resource ID: " + info.getId());
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ticket", ticketUrls);
        requestBody.put("developerId", RNetProtocol.DEVELOPER_KEY);
        requestBody.put("modelBody", resourceRequestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        URI fullUri;
        try {
            URI baseUri = new URI(info.getUrl());
            fullUri = baseUri.resolve("/ai/" + info.getId());
        } catch (URISyntaxException e) {
            throw new RNetException("Invalid URI: " + info.getUrl());
        }

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                fullUri,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> res = (Map<String, Object>) responseEntity.getBody();
        if (res == null) {
            throw new RNetException("Response body is empty from: " + info.getUrl());
        }

        String errorMsg = (String) res.get(RNetProtocol.RNET_ERROR);
        if (errorMsg != null) {
            String usageStr = (String) res.get(RNetProtocol.RNET_OUT);
            if (usageStr != null) {
                usage.add(info.getId(), usageStr);
            }
            throw new RNetException(errorMsg);
        }

        String usageStr = (String) res.get(RNetProtocol.RNET_OUT);
        if (usageStr == null) {
            throw new RNetException("'usage' field missing in response from: " + info.getName());
        }
        usage.add(info.getId(), usageStr);

        Object data = res.get("data");
        if (data == null) {
            throw new RNetException("'data' field missing in response from: " + info.getName());
        }

        if (!(data instanceof Map)) {
            throw new RNetException("'data' field is not a JSON object from: " + info.getName());
        }

        return (Map<String, Object>) data;
    }
}