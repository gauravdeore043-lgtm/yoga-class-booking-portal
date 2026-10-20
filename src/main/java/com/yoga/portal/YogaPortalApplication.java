package com.yoga.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extends SpringBootServletInitializer so the same WAR can be
 * deployed to an external Tomcat 10 server or started with {@code java -jar}.
 */
@SpringBootApplication
public class YogaPortalApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(YogaPortalApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(YogaPortalApplication.class, args);
    }
}
