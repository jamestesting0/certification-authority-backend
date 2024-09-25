package com.chuk_ca.sprint_initializer.service;

import org.springframework.core.io.Resource;

public interface ConfigFileLoaderInterface {
    Resource loadConfigFile(String fileName);
}
