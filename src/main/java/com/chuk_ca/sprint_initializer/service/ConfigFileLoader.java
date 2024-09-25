package com.chuk_ca.sprint_initializer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class ConfigFileLoader implements ConfigFileLoaderInterface {
    private static final String FILE_PATH_PREFIX = "classpath:static/config/";

    private final ResourceLoader resourceLoader;

    @Autowired
    public ConfigFileLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Resource loadConfigFile(String fileName) {
        return resourceLoader.getResource(FILE_PATH_PREFIX + fileName);
    }

    public Properties loadPropertiesFromFile(String fileName) throws IOException {
        Resource resource = loadConfigFile(fileName);

        Properties properties = new Properties();
        try (InputStream input = resource.getInputStream()) {
            properties.load(input);
        }

        return properties;
    }
}
