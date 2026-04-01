package io.github.rNetAi.rnetCore.rNetProtocol.advice;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.RNetResponse;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@ControllerAdvice
public class RNetProtocolResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {

        if(body instanceof RNetResponse){
            return body;
        }

        final HttpServletRequest request =
                ((ServletServerHttpRequest) serverHttpRequest).getServletRequest();

        final String requestPath = request.getServletPath();
        final boolean isRNetProtocolHeader = "true".equalsIgnoreCase(request.getHeader(RNetProtocol.RNET_HEADER));
        final boolean routeExists = RoutesContext.isPathPresent(requestPath);

        if (!(isRNetProtocolHeader && routeExists)) {
            return body;
        }

        String contentType = null;
        final RNetResponse res = new RNetResponse();
        res.usage = ((Usage) request.getAttribute(RNetProtocol.RNET_OUT)).getUsages();

        if(body instanceof String){
            contentType = "plain/text";
        }else{
            contentType = selectedContentType.toString();
        }

        res.metadata = Map.of(
                "statusCode", 200,
                "contentType" , contentType
        );
        serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if(body instanceof String){
            try {
                res.body = body;
                return OBJECT_MAPPER.writeValueAsString(res);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        try {
            res.body = OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}