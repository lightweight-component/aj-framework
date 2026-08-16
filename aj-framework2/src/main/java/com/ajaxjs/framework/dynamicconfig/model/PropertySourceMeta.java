package com.ajaxjs.framework.dynamicconfig.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.env.PropertySource;

import java.nio.file.Path;

/**
 * Metadata for a {@link PropertySource} loaded from the file system.
 */
@Data
@AllArgsConstructor
public class PropertySourceMeta {
    /**
     * The Spring {@link PropertySource} instance currently registered in the environment.
     */
    private PropertySource<?> propertySource;

    /**
     * The file path that backs this {@link PropertySource}.
     */
    private Path filePath;

    /**
     * Last modification timestamp (milliseconds since epoch) of {@link #filePath} when it was loaded.
     */
    private long lastModifyTime;
}
