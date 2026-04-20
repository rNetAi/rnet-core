package io.github.rNetAi.rnetCore.dto;

import io.github.rNetAi.rnetCore.entity.ModelInfo;
import io.github.rNetAi.rnetCore.entity.Route;

import java.util.Set;

public class InitConnectionDTO {
    private Set<Route> routes;
    private Set<ModelInfo> resources;

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<Route> routes) {
        this.routes = routes;
    }

    public Set<ModelInfo> getResources() {
        return resources;
    }

    public void setResources(Set<ModelInfo> resources) {
        this.resources = resources;
    }

    public InitConnectionDTO() {
    }
}