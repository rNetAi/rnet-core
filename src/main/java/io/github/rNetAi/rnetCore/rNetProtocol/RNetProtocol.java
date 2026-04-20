package io.github.rNetAi.rnetCore.rNetProtocol;

public class RNetProtocol {
    public final static String RNET_HEADER = "x-rNet-protocol";
    public final static String RNET_CENTRAL_SERVER = "http://localhost:9123";
    public final static String RNET_DEVELOPER_KEY_CHECK = "http://localhost:9123/developer/check";
    public final static String RNET_CENTRAL_AI_MODEL_CHECK = "http://localhost:9123/ai-model/check";

    public final static String RNET_IN = "tickets";
    public final static String RNET_OUT = "usage";
    public final static String RNET_ERROR = "error";
    public final static String RNET_ERROR_CODE = "errorCode";

    public static String DEVELOPER_KEY = null;
}
