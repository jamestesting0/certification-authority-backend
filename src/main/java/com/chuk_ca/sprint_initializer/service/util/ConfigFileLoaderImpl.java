package com.chuk_ca.sprint_initializer.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class ConfigFileLoaderImpl implements ConfigFileLoader {
    private static final String FILE_PATH_PREFIX = "classpath:config/";
    private final ResourceLoader resourceLoader;

    @Autowired
    public ConfigFileLoaderImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Resource loadConfigFile(String fileName) {
        return resourceLoader.getResource(FILE_PATH_PREFIX + fileName);
    }

    @Override
    public Properties loadPropertiesFromFile(String fileName) throws IOException {
        Resource resource = loadConfigFile(fileName);

        Properties properties = new Properties();
        try (InputStream input = resource.getInputStream()) {
            properties.load(input);
        }

        return properties;
    }
}
