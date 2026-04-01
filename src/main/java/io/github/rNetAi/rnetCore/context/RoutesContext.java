package io.github.rNetAi.rnetCore.context;

import io.github.rNetAi.rnetCore.entity.Route;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.*;

public class RoutesContext {
    private final static PathPatternParser parser = new PathPatternParser();
    private final static Map<String, List<RouteEntry>> routes = new HashMap<>();

    private final static Set<Route> allRoutePaths = new HashSet<>();
    private final static Set<PathPattern> allPathPatterns = new HashSet<>();

    public static void addRoute(String path,
                                String method,
                                Set<Long> resources) {

        PathPattern pattern = parser.parse(path);

        routes
                .computeIfAbsent(method, k -> new ArrayList<>())
                .add(new RouteEntry(pattern, resources));
        allRoutePaths.add(new Route(path , method));
        allPathPatterns.add(pattern);
    }

    public static Set<Route> getAllPaths() {
        return allRoutePaths;
    }

    public static Set<Long> match(String path, String method) {
        List<RouteEntry> entries = routes.get(method);

        if (entries == null)
            return null;

        PathContainer container = PathContainer.parsePath(path);

        for (RouteEntry entry : entries) {

            var result = entry.pattern.matches(container);

            if (result) {
                return entry.resourceIds;
            }
        }

        return null;
    }

    public static boolean matchPresent(String path, String method) {
        List<RouteEntry> entries = routes.get(method);

        if (entries == null)
            return false;

        PathContainer container = PathContainer.parsePath(path);

        for (RouteEntry entry : entries) {
            if (entry.pattern.matches(container)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPathPresent(String path) {
        PathContainer container = PathContainer.parsePath(path);

        for(PathPattern pattern : allPathPatterns) {
            if (pattern.matches(container)) {
                return true;
            }
        }

        return false;
    }

    static class RouteEntry {

        PathPattern pattern;
        Set<Long> resourceIds;

        RouteEntry(PathPattern pattern,
                   Set<Long> resourceIds) {

            this.pattern = pattern;
            this.resourceIds = resourceIds;
        }

        @Override
        public String toString() {
            return "RouteEntry{" +
                    "pattern=" + pattern +
                    ", resources=" + resourceIds +
                    '}';
        }
    }
}
