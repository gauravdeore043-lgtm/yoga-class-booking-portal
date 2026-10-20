package com.yoga.portal.web;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds values every page needs (version, environment) to the model. */
@ControllerAdvice(basePackages = "com.yoga.portal.web")
public class GlobalModelAdvice {

    private final ObjectProvider<BuildProperties> buildProperties;
    private final String environment;

    public GlobalModelAdvice(ObjectProvider<BuildProperties> buildProperties,
                             @Value("${app.environment:local}") String environment) {
        this.buildProperties = buildProperties;
        this.environment = environment;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        BuildProperties props = buildProperties.getIfAvailable();
        return props != null ? props.getVersion() : "dev";
    }

    @ModelAttribute("appEnvironment")
    public String appEnvironment() {
        return environment;
    }
}
