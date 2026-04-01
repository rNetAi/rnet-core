package io.github.rNetAi.rnetCore.context;

import io.github.rNetAi.rnetCore.entity.ResourceInfo;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetResource;

import java.util.*;

public final class ResourceContext {
    private final static Map<Long , ResourceInfo> resourceInfoMap =  new HashMap<Long, ResourceInfo>();

    private final static Map<Class<? extends RNetResource> , Long> mapWithClass = new HashMap<>();

    public static void putIfAbsent(Set<ResourceInfo> resourceInfoSet) {
        for (ResourceInfo resourceInfo : resourceInfoSet) {
            resourceInfoMap.putIfAbsent(resourceInfo.getId(), resourceInfo);
        }
    }

    public static void putIfAbsent(Iterator<ResourceInfo> resourceInfos) {
        resourceInfos.forEachRemaining(resourceInfo -> {
           resourceInfoMap.putIfAbsent(resourceInfo.getId(), resourceInfo);
        });
    }

    public static Set<ResourceInfo> getResourceInfoSet(Set<Long> resourceIds) {
        if(resourceIds == null || resourceIds.isEmpty()) {
            return null;
        }
        Set<ResourceInfo> resourceInfoSet = new HashSet<ResourceInfo>(resourceIds.size());
        resourceIds.forEach(resourceId -> resourceInfoSet.add(resourceInfoMap.get(resourceId)));
        return resourceInfoSet;
    }

    public static void putClassMappingIfAbsent(Class<? extends RNetResource> classInfo , Long id) {
        mapWithClass.putIfAbsent(classInfo,id);
    }

    public static ResourceInfo getClassMapping(Class<? extends RNetResource> classInfo) {
        Long id = mapWithClass.get(classInfo);
        if (id == null) {
            return null;
        }
        return resourceInfoMap.get(id);
    }
}
