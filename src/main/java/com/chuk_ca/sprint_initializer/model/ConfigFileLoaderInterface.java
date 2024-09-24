package com.chuk_ca.sprint_initializer.model;

import org.springframework.core.io.Resource;

public interface ConfigFileLoaderInterface {
    Resource loadConfigFile(String fileName);
}
