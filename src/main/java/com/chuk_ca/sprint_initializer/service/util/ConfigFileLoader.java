package com.chuk_ca.sprint_initializer.service.util;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

public interface ConfigFileLoader {
    Resource loadConfigFile(String fileName);

    Properties loadPropertiesFromFile(String fileName) throws IOException;
}
