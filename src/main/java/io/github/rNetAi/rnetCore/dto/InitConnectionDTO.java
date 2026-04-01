package io.github.rNetAi.rnetCore.dto;

import io.github.rNetAi.rnetCore.entity.ResourceInfo;
import io.github.rNetAi.rnetCore.entity.Route;

import java.util.Set;

public class InitConnectionDTO {
    private Set<Route> routes;
    private Set<ResourceInfo> resources;

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<Route> routes) {
        this.routes = routes;
    }

    public Set<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(Set<ResourceInfo> resources) {
        this.resources = resources;
    }

    public InitConnectionDTO() {
    }
}