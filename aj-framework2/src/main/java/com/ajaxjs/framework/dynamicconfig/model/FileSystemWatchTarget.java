package com.ajaxjs.framework.dynamicconfig.model;

import com.ajaxjs.framework.dynamicconfig.ConfigurationUtils;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.ajaxjs.framework.dynamicconfig.ConfigurationUtils.CONFIG_FILE_PREFIX;

/**
 * Directory WatchService target, could be a file or directory
 */
@Data
public class FileSystemWatchTarget {
    /**
     * The origin of this watch target (spring.config.location / spring.config.import).
     */
    private WatchTargetType type;

    /**
     * Normalized directory path used as a stable key for watch registration.
     */
    private String normalizedDir;

    /**
     * Optional file-name filters when a directory is watched but only a subset of files should trigger reload.
     */
    private List<String> filterFiles;

    /**
     * Root directory of a config tree when {@link WatchTargetType#CONFIG_IMPORT_TREE} is used.
     */
    private Path rootDir;

    /**
     * Build a watch target from the raw configuration string.
     *
     * @param type         the target type
     * @param originalPath the raw configured path, may contain {@code file:} prefix
     */
    public FileSystemWatchTarget(WatchTargetType type, String originalPath) {
        this.type = type;

        if (originalPath.startsWith(CONFIG_FILE_PREFIX))
            originalPath = ConfigurationUtils.trimRelativePathAndReplaceBackSlash(originalPath.substring(CONFIG_FILE_PREFIX.length()));
         else
            originalPath = ConfigurationUtils.trimRelativePathAndReplaceBackSlash(originalPath);

        if (type == WatchTargetType.CONFIG_LOCATION)
            this.normalizedDir = originalPath;
        else if (type == WatchTargetType.CONFIG_IMPORT_FILE) {
            int idx = originalPath.lastIndexOf("/");
            this.normalizedDir = originalPath.substring(0, idx);
            this.filterFiles = new ArrayList<>(2);
            this.filterFiles.add(originalPath.substring(idx + 1));
        } else if (type == WatchTargetType.CONFIG_IMPORT_TREE)
            this.normalizedDir = originalPath;
    }
}
