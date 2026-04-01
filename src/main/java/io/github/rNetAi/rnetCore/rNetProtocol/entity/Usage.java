package io.github.rNetAi.rnetCore.rNetProtocol.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Usage {
    private final Map<Long, List<String>> usages;

    public Usage(int size) {
        this.usages = new ConcurrentHashMap<>(size);
    }

    public void add(long id , String usage){
        usages.computeIfAbsent(id, k -> new ArrayList<>())
                .add(usage);
    }

    public Map<Long , List<String>> getUsages(){
        return usages;
    }

    @Override
    public String toString() {
        return "Usage{" +
                "usages=" + usages +
                '}';
    }
}
