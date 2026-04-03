//package io.github.rNetAi.rnetCore.scanner;
//
//import io.github.classgraph.ClassGraph;
//import io.github.classgraph.ScanResult;
//import io.github.rNetAi.rnetCore.context.ResourceContext;
//import io.github.rNetAi.rnetCore.context.RoutesContext;
//import io.github.rNetAi.rnetCore.entity.ResourceInfo;
//import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
//import io.github.rNetAi.rnetCore.rNetProtocol.RNetResource;
//import io.github.rNetAi.rnetCore.scanner.annotations.RNetEndpoint;
//import io.github.rNetAi.rnetCore.scanner.annotations.Resource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.core.annotation.AnnotatedElementUtils;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.lang.reflect.Method;
//import java.util.*;
//
//public class RNetAnnotationScanner {
//
//    private static final Logger log = LoggerFactory.getLogger(RNetAnnotationScanner.class);
//
//    private final Map<String, Map<String, Set<String>>> registry = new HashMap<>();
//    private final Set<String> allResources = new HashSet<>();
//    private final Map<String, Class<? extends RNetResource>> resourcesClass = new HashMap<>();
//
//    public void scan() {
//
//        log.info("Starting RNet annotation scan...");
//
//        scanControllers();
//
//        log.info("Usage Resources: {}", allResources);
//
//        if (allResources.isEmpty()) {
//            log.warn("No resources found, skipping further processing");
//            return;
//        }
//
//        Map<String, ResourceInfo> resourceInfoMap = fetchResourceInfo();
//
//        validateResources(resourceInfoMap);
//
//        buildResourceCollection(resourceInfoMap);
//
//        log.debug("Final Registry: {}", registry);
//
//        registry.clear();
//        allResources.clear();
//        resourcesClass.clear();
//
//        log.info("RNet annotation scan completed");
//    }
//
//    /**
//     * Scan controllers
//     */
//    private void scanControllers() {
//
//        try (ScanResult scanResult =
//                     new ClassGraph()
//                             .enableClassInfo()
//                             .enableMethodInfo()
//                             .enableAnnotationInfo()
//                             .scan()) {
//
//            scanResult
//                    .getClassesWithMethodAnnotation(RNetEndpoint.class)
//                    .forEach(classInfo -> {
//                        processController(classInfo.loadClass());
//                    });
//        }
//    }
//
//    /**
//     * Call resource manager
//     */
//    private Map<String, ResourceInfo> fetchResourceInfo() {
//
//        try {
//
//            log.info("Fetching resource info from central server...");
//
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<Map<String, ResourceInfo>> response =
//                    restTemplate.exchange(
//                            RNetProtocol.RNET_CENTRAL_SERVER_RESOURCE,
//                            HttpMethod.POST,
//                            new HttpEntity<>(allResources),
//                            new ParameterizedTypeReference<>() {}
//                    );
//
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                log.error("❌ Resource manager unavailable. Status: {}", response.getStatusCode());
//                throw new RuntimeException("Resource manager unavailable");
//            }
//
//
//            return Objects.requireNonNull(response.getBody());
//
//        } catch (Exception e) {
//            log.error("Error while fetching resource info", e);
//            throw new IllegalStateException("RNet initialization failed");
//        }
//    }
//
//    /**
//     * Validate resources
//     */
//    private void validateResources(Map<String, ResourceInfo> resourceInfoMap) {
//
//        List<String> notFound = new ArrayList<>();
//
//        resourceInfoMap.forEach((name, info) -> {
//            if (info == null) notFound.add(name);
//        });
//
//        if (!notFound.isEmpty()) {
//            log.error("Resource not found: {}", notFound);
//            throw new IllegalStateException("RNet initialization failed");
//        } else {
//            log.info("All resources validated successfully");
//        }
//    }
//
//    /**
//     * Build EndpointMatcher map
//     */
//    private void buildResourceCollection(Map<String, ResourceInfo> resourceInfoMap) {
//
//        log.debug("Building resource collection...");
//
//        ResourceContext.putIfAbsent(resourceInfoMap.values().iterator());
//
//        resourceInfoMap.forEach((name, info) -> {
//            ResourceContext.putClassMappingIfAbsent(resourcesClass.get(name), info.getId());
//        });
//
//        registry.forEach((url, methodMap) -> {
//
//            methodMap.forEach((httpMethod, resourceNames) -> {
//
//                Set<Long> resourceIds = new HashSet<>(resourceNames.size());
//
//                resourceNames.forEach(name -> {
//                    resourceIds.add(resourceInfoMap.get(name).getId());
//                });
//
//                log.debug("Mapping route: {} {} -> {}", httpMethod, url, resourceIds);
//
//                RoutesContext.addRoute(url, httpMethod.toUpperCase(), resourceIds);
//
//            });
//        });
//    }
//
//    /**
//     * Process controller
//     */
//    private void processController(Class<?> controllerClass) {
//
//        List<String> classPaths = getClassPaths(controllerClass);
//
//        for (Method method : controllerClass.getDeclaredMethods()) {
//
//            if (!method.isAnnotationPresent(RNetEndpoint.class))
//                continue;
//
//            log.debug("Processing method: {}#{}", controllerClass.getSimpleName(), method.getName());
//
//            processMethod(method, classPaths);
//        }
//    }
//
//    /**
//     * Class level mapping
//     */
//    private List<String> getClassPaths(Class<?> controllerClass) {
//
//        RequestMapping mapping =
//                controllerClass.getAnnotation(RequestMapping.class);
//
//        if (mapping != null) {
//
//            List<String> paths = resolvePaths(mapping.value(), mapping.path());
//
//            if (!paths.isEmpty())
//                return paths;
//        }
//
//        return List.of("");
//    }
//
//    /**
//     * Process method
//     */
//    private void processMethod(Method method, List<String> classPaths) {
//
//        List<String> methodPaths = getMethodPaths(method);
//        String httpMethod = getHttpMethod(method);
//        Set<String> resourceNames = getResourceNames(method);
//
//        for (String cp : classPaths) {
//
//            for (String mp : methodPaths) {
//
//                String fullUrl = cp + mp;
//
//                registry
//                        .computeIfAbsent(fullUrl, k -> new HashMap<>())
//                        .put(httpMethod, resourceNames);
//            }
//        }
//    }
//
//
//    private List<String> getMethodPaths(Method method) {
//
//        RequestMapping m = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
//
//        if (m != null) {
//            return resolvePaths(m.value(), m.path());
//        }
//
//        return List.of("");
//    }
//
//    /**
//     * Extract HTTP method
//     */
//    private String getHttpMethod(Method method) {
//
//        RequestMapping m =
//                AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
//
//        if (m != null) {
//            RequestMethod[] methods = m.method();
//            return methods.length > 0 ? methods[0].name() : "ALL";
//        }
//
//        return "UNKNOWN";
//    }
//
//    private Set<String> getResourceNames(Method method) {
//
//        RNetEndpoint annotation =
//                AnnotatedElementUtils.findMergedAnnotation(method, RNetEndpoint.class);
//
//        Set<String> resourceNames = new HashSet<>();
//
//        if (annotation == null) {
//            return resourceNames;
//        }
//
//        for (Class<? extends RNetResource> resourceClass : annotation.usageResources()) {
//
//            Resource resourceAnnotation =
//                    resourceClass.getAnnotation(Resource.class);
//
//            if (resourceAnnotation != null) {
//
//                String name = resourceAnnotation.value();
//
//                resourceNames.add(name);
//                allResources.add(name);
//
//                resourcesClass.putIfAbsent(name, resourceClass);
//            }
//        }
//
//        return resourceNames;
//    }
//
//    private List<String> resolvePaths(String[] value, String[] path) {
//
//        if (value != null && value.length > 0)
//            return Arrays.asList(value);
//
//        if (path != null && path.length > 0)
//            return Arrays.asList(path);
//
//        return List.of("");
//    }
//}


package io.github.rNetAi.rnetCore.scanner;

import io.github.rNetAi.rnetCore.context.ResourceContext;
import io.github.rNetAi.rnetCore.context.RoutesContext;
import io.github.rNetAi.rnetCore.entity.ResourceInfo;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetResource;
import io.github.rNetAi.rnetCore.scanner.annotations.RNetEndpoint;
import io.github.rNetAi.rnetCore.scanner.annotations.Resource;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Component
public class RNetSpringScanner {

    private final RequestMappingHandlerMapping handlerMapping;
    private static final Logger log = LoggerFactory.getLogger(RNetSpringScanner.class);


    private final Map<String, Map<String, Set<String>>> registry = new HashMap<>();
    private final Set<String> allResources = new HashSet<>();
    private final Map<String, Class<? extends RNetResource>> resourcesClass = new HashMap<>();

    public RNetSpringScanner(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    public final void scan() {

        handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {

            RNetEndpoint annotation =
                    AnnotatedElementUtils.findMergedAnnotation(
                            handlerMethod.getMethod(),
                            RNetEndpoint.class
                    );

            if (annotation == null) return;

            Set<String> paths = mappingInfo.getPatternValues();
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

            if (methods.isEmpty()) {
                methods = Set.of(RequestMethod.GET); // default
            }

            Set<String> resourceNames = extractResources(annotation);

            for (String path : paths) {
                for (RequestMethod method : methods) {

                    registry
                            .computeIfAbsent(path, k -> new HashMap<>())
                            .put(method.name(), resourceNames);
                }
            }
        });
        // 🔥 Now reuse your existing pipeline
        processRegistry();
    }

    private Set<String> extractResources(RNetEndpoint annotation) {

        Set<String> resourceNames = new HashSet<>();

        for (Class<? extends RNetResource> resourceClass : annotation.usageResources()) {

            Resource resourceAnnotation =
                    resourceClass.getAnnotation(Resource.class);

            if (resourceAnnotation != null) {

                String name = resourceAnnotation.value();

                resourceNames.add(name);
                allResources.add(name);

                resourcesClass.putIfAbsent(name, resourceClass);
            }
        }

        return resourceNames;
    }


    private void processRegistry() {

        if (allResources.isEmpty()) return;

        Map<String, ResourceInfo> resourceInfoMap = fetchResourceInfo();

        validateResources(resourceInfoMap);

        buildResourceCollection(resourceInfoMap);
    }

    /**
     * Call resource manager
     */
    private Map<String, ResourceInfo> fetchResourceInfo() {

        try {

            log.info("Fetching resource info from central server...");

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map<String, ResourceInfo>> response =
                    restTemplate.exchange(
                            RNetProtocol.RNET_CENTRAL_SERVER_RESOURCE,
                            HttpMethod.POST,
                            new HttpEntity<>(allResources),
                            new ParameterizedTypeReference<>() {}
                    );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ Resource manager unavailable. Status: {}", response.getStatusCode());
                throw new RuntimeException("Resource manager unavailable");
            }


            return Objects.requireNonNull(response.getBody());

        } catch (Exception e) {
            log.error("Error while fetching resource info", e);
            throw new IllegalStateException("RNet initialization failed");
        }
    }

    /**
     * Validate resources
     */
    private void validateResources(Map<String, ResourceInfo> resourceInfoMap) {

        List<String> notFound = new ArrayList<>();

        resourceInfoMap.forEach((name, info) -> {
            if (info == null) notFound.add(name);
        });

        if (!notFound.isEmpty()) {
            log.error("Resource not found: {}", notFound);
            throw new IllegalStateException("RNet initialization failed");
        } else {
            log.info("All resources validated successfully");
        }
    }

    /**
     * Build EndpointMatcher map
     */
    private void buildResourceCollection(Map<String, ResourceInfo> resourceInfoMap) {

        log.debug("Building resource collection...");

        ResourceContext.putIfAbsent(resourceInfoMap.values().iterator());

        resourceInfoMap.forEach((name, info) -> {
            ResourceContext.putClassMappingIfAbsent(resourcesClass.get(name), info.getId());
        });

        registry.forEach((url, methodMap) -> {

            methodMap.forEach((httpMethod, resourceNames) -> {

                Set<Long> resourceIds = new HashSet<>(resourceNames.size());

                resourceNames.forEach(name -> {
                    resourceIds.add(resourceInfoMap.get(name).getId());
                });

                log.debug("Mapping route: {} {} -> {}", httpMethod, url, resourceIds);

                RoutesContext.addRoute(url, httpMethod.toUpperCase(), resourceIds);

            });
        });
    }
}