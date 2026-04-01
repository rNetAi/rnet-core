package io.github.rNetAi.rnetCore.rNetProtocol.advice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.RNetResponse;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import io.github.rNetAi.rnetCore.rNetProtocol.exception.RNetException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RNetProtocolExceptionHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ExceptionHandler(RNetException.class)
    public RNetResponse handleRNetException(RNetException ex , HttpServletRequest request) throws JsonProcessingException {

        final boolean isRNetProtocolHeader = "true".equalsIgnoreCase(request.getHeader(RNetProtocol.RNET_HEADER));
        final boolean routeExists = RoutesContext.isPathPresent(request.getServletPath());

        if (!(isRNetProtocolHeader && routeExists)) {
            throw new RuntimeException(ex);
        }

        final RNetResponse res = new RNetResponse();
        res.body = OBJECT_MAPPER.writeValueAsString(ex.getExceptionMap());
        res.metadata = Map.of(
                "statusCode", 500,
                "contentType", "application/json"
        );

        res.usage = ((Usage) request.getAttribute(RNetProtocol.RNET_OUT)).getUsages();
        return res;
    }
}