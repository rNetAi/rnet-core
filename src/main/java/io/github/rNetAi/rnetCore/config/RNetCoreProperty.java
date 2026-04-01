package io.github.rNetAi.rnetCore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rnet")
public class RNetCoreProperty {

    private Developer developer = new Developer();

    public static class Developer {
        private String key;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }

    public Developer getDeveloper() { return developer; }
    public void setDeveloper(Developer developer) { this.developer = developer; }
}
