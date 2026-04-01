package io.github.rNetAi.rnetCore.rNetProtocol.exception;

import java.util.HashMap;
import java.util.Map;

public class RNetException extends RuntimeException {
    public static final String REQUIRED_RNET_EXCEPTION_FIELD = "message";

    private final Map<String , Object> exceptionMap;

    public RNetException(Map<String , Object> map) {
        super(map.getOrDefault(REQUIRED_RNET_EXCEPTION_FIELD, "Required Exception field is not set.").toString());
        this.exceptionMap = map;
    }

    public RNetException(String message) {
        super(message);
        this.exceptionMap = new HashMap<>();
        exceptionMap.put(REQUIRED_RNET_EXCEPTION_FIELD, message);
    }

    public Map<String, Object> getExceptionMap() {
        return exceptionMap;
    }
}
