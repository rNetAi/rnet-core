package io.github.rNetAi.rnetCore.autoconfigure;

import io.github.rNetAi.rnetCore.config.RNetCoreProperty;
import io.github.rNetAi.rnetCore.init.RNetCoreInitializer;
import io.github.rNetAi.rnetCore.rNetProtocol.filter.RNetGlobalRequestFilter;
import io.github.rNetAi.rnetCore.web.RNetProtocolEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "rnet", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(RNetCoreProperty.class)
@Import(RNetProtocolEndpoint.class)
public class RNetCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RNetCoreInitializer rNetCoreInitializer(RNetCoreProperty property) {
        return new RNetCoreInitializer(property);
    }

    @Bean
    public OncePerRequestFilter rNetGlobalRequestFilter() {
        return new RNetGlobalRequestFilter();
    }
}