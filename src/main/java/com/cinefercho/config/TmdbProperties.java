package com.cinefercho.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmdb.api")
public record TmdbProperties(String key, String url) {

    public boolean enabled() {
        return key != null && !key.isBlank();
    }
}
