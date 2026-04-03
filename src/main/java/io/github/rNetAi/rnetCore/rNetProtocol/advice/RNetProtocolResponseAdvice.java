package io.github.rNetAi.rnetCore.rNetProtocol.advice;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.RNetResponse;
import io.github.rNetAi.rnetCore.rNetProtocol.entity.Usage;
import io.github.rNetAi.rnetCore.scanner.annotations.RNetResponseEnabled;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
public final class RNetProtocolResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(RNetProtocolResponseAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        final boolean methodAnnotated =
                returnType.hasMethodAnnotation(RNetResponseEnabled.class);
        if (!methodAnnotated) {
            return false;
        }

        if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType)) {
            return true;
        }

        final String methodName = returnType.getMethod() != null
                ? returnType.getMethod().getName()
                : "unknown";

        log.error("@RNetResponseEnabled method '{}' in '{}' must return a JSON-serializable object (got converter: {})",
                methodName,
                returnType.getDeclaringClass().getSimpleName(),
                converterType.getSimpleName()
        );

        throw new RuntimeException("Non-JSON converter for @RNetResponseEnabled method: " + methodName);
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

        String contentType = null;
        final RNetResponse res = new RNetResponse();
        res.usage = ((Usage) request.getAttribute(RNetProtocol.RNET_OUT)).getUsages();

        if(res.usage == null) throw new RuntimeException("Usage must not be null");

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