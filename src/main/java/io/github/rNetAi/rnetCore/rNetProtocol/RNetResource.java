package io.github.rNetAi.rnetCore.rNetProtocol;

import io.github.rNetAi.rnetCore.context.ResourceContext;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.entity.ResourceInfo;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Tickets;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import io.github.rNetAi.rnetCore.rNetProtocol.exception.RNetException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class RNetResource {

    @SuppressWarnings("unchecked")
    public final Map<String, Object> call(
            @Nonnull HttpServletRequest req,
            @Nonnull RestTemplate restTemplate,
            Map<String, Object> resourceRequestBody
    ) throws ServletException, URISyntaxException {

        if (!RoutesContext.isPathPresent(req.getServletPath())) {
            throw new IllegalCallerException("Path is not registered for AI call");
        }

        Tickets tickets = (Tickets) req.getAttribute(RNetProtocol.RNET_IN);
        if (tickets == null) {
            throw new IllegalStateException("tickets is missing in request attributes");
        }

        Class<? extends RNetResource> resourceProcessorClass =
                (Class<? extends RNetResource>) ClassUtils.getUserClass(this);
        ResourceInfo info = ResourceContext.getClassMapping(resourceProcessorClass);
        if (info == null) {
            throw new IllegalStateException("ResourceInfo not found for class " + resourceProcessorClass.getName());
        }

        List<String> ticketUrls = tickets.getResourceTicket(info.getId());
        if (ticketUrls == null) {
            throw new IllegalStateException("No tickets found for Resource ID " + info.getId());
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ticket", ticketUrls);
        requestBody.put("id", "developer's login id");
        requestBody.put("modelBody", resourceRequestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        URI baseUri = new URI(info.getUrl());
        URI fullUri = baseUri.resolve("/ai?modelId=" + info.getId());

        restTemplate.setErrorHandler(response -> false);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    fullUri,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if(responseEntity.getStatusCode().is4xxClientError()){
                throw new RNetException(Objects.requireNonNull(responseEntity.getBody()));
            }

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new ServletException(Objects.requireNonNull(responseEntity.getBody()).get("message").toString());
            }

            Map<String, Object> res = (Map<String, Object>) responseEntity.getBody();
            if (res == null) {
                throw new ServletException("Response body is null from " + info.getName());
            }
            Usage usage = (Usage) req.getAttribute(RNetProtocol.RNET_OUT);

            if (usage == null) {
                throw new IllegalStateException("usage var is missing in request attributes");
            }

            usage.add(info.getId(), (String) res.get(RNetProtocol.RNET_OUT));

            return (Map<String, Object>) res.get("data");
    }
}