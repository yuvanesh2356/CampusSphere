package com.campussphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps the /uploads/** URL path to the local filesystem directory
 * configured by campussphere.upload.dir, so images saved by
 * FileStorageService are servable back out to the browser without
 * being bundled into the classpath/build artifact.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${campussphere.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
