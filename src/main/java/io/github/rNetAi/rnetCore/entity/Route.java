package io.github.rNetAi.rnetCore.entity;

public class Route {
    private final String path;
    private final String method;

    public Route(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public String path() {
        return path;
    }

    public String method() {
        return method;
    }

    @Override
    public String toString() {
        return "Route[path=" + path + ", method=" + method + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route other = (Route) o;
        return java.util.Objects.equals(path, other.path) &&
                java.util.Objects.equals(method, other.method);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(path, method);
    }
}
