package dev.runtime_lab.flowit.global.config;

import dev.runtime_lab.flowit.domain.file.storage.LocalProfileImageStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocalProfileImageStorageProperties.class)
public class StorageConfig {
}
