package io.github.rNetAi.rnetCore.scanner.annotations;

import io.github.rNetAi.rnetCore.rNetProtocol.RNetResource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@RNetResponseEnabled
public @interface RNetEndpoint {

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path();

    @AliasFor(annotation = RequestMapping.class, attribute = "method")
    RequestMethod[] method();

    Class<? extends RNetResource>[] usageResources();
}