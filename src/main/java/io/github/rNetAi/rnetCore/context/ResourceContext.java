package io.github.rNetAi.rnetCore.context;

import io.github.rNetAi.rnetCore.entity.ModelInfo;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetResource;

import java.util.*;

public final class ResourceContext {
    private final static Map<Long , ModelInfo> resourceInfoMap =  new HashMap<Long, ModelInfo>();

    private final static Map<Class<? extends RNetResource> , Long> mapWithClass = new HashMap<>();

    public static void putIfAbsent(Set<ModelInfo> modelInfoSet) {
        for (ModelInfo modelInfo : modelInfoSet) {
            resourceInfoMap.putIfAbsent(modelInfo.getId(), modelInfo);
        }
    }

    public static void putIfAbsent(Iterator<ModelInfo> resourceInfos) {
        resourceInfos.forEachRemaining(resourceInfo -> {
           resourceInfoMap.putIfAbsent(resourceInfo.getId(), resourceInfo);
        });
    }

    public static Set<ModelInfo> getResourceInfoSet(Set<Long> resourceIds) {
        if(resourceIds == null || resourceIds.isEmpty()) {
            return null;
        }
        Set<ModelInfo> modelInfoSet = new HashSet<ModelInfo>(resourceIds.size());
        resourceIds.forEach(resourceId -> modelInfoSet.add(resourceInfoMap.get(resourceId)));
        return modelInfoSet;
    }

    public static void putClassMappingIfAbsent(Class<? extends RNetResource> classInfo , Long id) {
        mapWithClass.putIfAbsent(classInfo,id);
    }

    public static ModelInfo getClassMapping(Class<? extends RNetResource> classInfo) {
        Long id = mapWithClass.get(classInfo);
        if (id == null) {
            return null;
        }
        return resourceInfoMap.get(id);
    }
}
