package com.seasungames.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by jianghaitao on 2020/4/23.
 */
@ConfigProperties(prefix = "api")
public interface ApiConfig {
    @ConfigProperty(name = "page-size", defaultValue = "10")
    int pageSize();

    @ConfigProperty(name = "secret-key", defaultValue = "test")
    String secretKey();

    @ConfigProperty(name = "request.content-type", defaultValue = "application/json")
    String requestContentType();

    @ConfigProperty(name = "response.content-type", defaultValue = "application/json")
    String responseContentType();
}
