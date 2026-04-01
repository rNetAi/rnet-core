package io.github.rNetAi.rnetCore.rNetProtocol.entity;

public class RNetResponse {
    public Object body;
    public Object metadata;
    public Object usage;

    @Override
    public String toString() {
        return "RNetResponse{" +
                "body=" + body +
                ", metadata=" + metadata +
                ", usage=" + usage +
                '}';
    }
}