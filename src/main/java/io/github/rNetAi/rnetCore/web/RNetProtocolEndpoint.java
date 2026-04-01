package io.github.rNetAi.rnetCore.web;

import io.github.rNetAi.rnetCore.context.ResourceContext;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.dto.InitConnectionDTO;
import io.github.rNetAi.rnetCore.entity.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequestMapping("/resource")
public class RNetProtocolEndpoint {

    @GetMapping("/paths")
    public ResponseEntity<InitConnectionDTO> getRoutes(
            @RequestParam(name = "httpMethod", required = false) String httpMethod,
            @RequestParam(name = "path", required = false) String path) {

        if ((httpMethod == null && path != null) || (httpMethod != null && path == null)) {
            return ResponseEntity.badRequest().build();
        }

        InitConnectionDTO data = new InitConnectionDTO();
        final Set<Route> paths = RoutesContext.getAllPaths();
        data.setRoutes(paths);

        if (httpMethod != null) {
            final String decodedUrl = URLDecoder.decode(path, StandardCharsets.UTF_8);
            final Set<Long> resourceIds =
                    RoutesContext.match(decodedUrl, httpMethod.toUpperCase());
            data.setResources(ResourceContext.getResourceInfoSet(resourceIds));
        }

        if (paths.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/")
    public ResponseEntity<Set<Long>> getAllResources(
            @RequestParam(name = "httpMethod") String httpMethod,
            @RequestParam(name = "path") String path) {

        final String decodedUrl = URLDecoder.decode(path, StandardCharsets.UTF_8);
        final Set<Long> resourceIds =
                RoutesContext.match(decodedUrl, httpMethod.toUpperCase());

        if (resourceIds == null || resourceIds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resourceIds);
    }
}