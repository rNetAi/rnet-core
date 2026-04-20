package io.github.rNetAi.rnetCore.autoconfigure;

import io.github.rNetAi.rnetCore.config.RNetCoreProperty;
import io.github.rNetAi.rnetCore.init.RNetCoreInitializer;
import io.github.rNetAi.rnetCore.rNetProtocol.advice.RNetProtocolExceptionHandler;
import io.github.rNetAi.rnetCore.rNetProtocol.advice.RNetProtocolResponseAdvice;
import io.github.rNetAi.rnetCore.rNetProtocol.filter.RNetGlobalRequestFilter;
import io.github.rNetAi.rnetCore.scanner.RNetSpringScanner;
import io.github.rNetAi.rnetCore.web.RNetProtocolEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "rnet", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(RNetCoreProperty.class)
@Import(RNetProtocolEndpoint.class)
public class RNetCoreAutoConfiguration {

    @Bean
    public RNetCoreInitializer rNetCoreInitializer(RNetSpringScanner rNetSpringScanner, RNetCoreProperty property ) {
        return new RNetCoreInitializer(rNetSpringScanner , property);
    }

    @Bean
    public RNetSpringScanner rNetSpringScanner(RequestMappingHandlerMapping handlerMapping) {
        return new RNetSpringScanner(handlerMapping);
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public RNetProtocolResponseAdvice rNetProtocolResponseAdvice() {
        return new RNetProtocolResponseAdvice();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RNetProtocolExceptionHandler rNetProtocolExceptionHandler() {
        return new RNetProtocolExceptionHandler();
    }

    @Bean
    public FilterRegistrationBean<RNetGlobalRequestFilter> rNetGlobalRequestFilterRegistration() {
        FilterRegistrationBean<RNetGlobalRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RNetGlobalRequestFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}