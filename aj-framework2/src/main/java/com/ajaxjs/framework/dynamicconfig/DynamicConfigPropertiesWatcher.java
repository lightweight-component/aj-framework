package com.ajaxjs.framework.dynamicconfig;

import com.ajaxjs.framework.dynamicconfig.model.ConfigurationChangedEvent;
import com.ajaxjs.framework.dynamicconfig.model.FileSystemWatchTarget;
import com.ajaxjs.framework.dynamicconfig.model.PropertySourceMeta;
import com.ajaxjs.framework.dynamicconfig.model.WatchTargetType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.SpringFactoriesLoader;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 * Enhance PropertySource when spring.config.location is specified, it will start directory-watch,
 * listening any changes on configuration files, then publish ConfigurationChangedEvent.
 * Support config import feature since Spring Boot 2.4, check following link for further info:
 * <a href="https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#features.external-config.files.configtree">...</a>
 *
 * @author Code2Life
 * @see ConfigurationChangedEvent
 */
@Slf4j
@ConditionalOnExpression("T(top.code2life.config.ConfigurationUtils).hasWatchableConf('${spring.config.location:}', '${spring.config.import:}')")
public class DynamicConfigPropertiesWatcher implements DisposableBean {
    /**
     * Watch targets parsed from {@code spring.config.location} and {@code spring.config.import}.
     */
    static final Map<String, FileSystemWatchTarget> WATCHABLE_TARGETS = new HashMap<>(4);

    /**
     * Polling interval (milliseconds) for Kubernetes ConfigMap/Secret symbolic-link changes.
     */
    private static final long SYMBOL_LINK_POLLING_INTERVAL = 5000;
    /**
     * Periodic polling interval (milliseconds) used as a fallback when WatchService misses events.
     */
    private static final long NORMAL_FILE_POLLING_INTERVAL = 90000;

    /**
     * Prefix marker used in file-based configuration strings.
     */
    private static final String FILE_COLON_SYMBOL = "file:";

    /**
     * Kubernetes will inject data when mounting configMap or secret, it's not watchable symbol link
     */
    private static final String HIDDEN_SYMBOL_LINK_DIR = "..data";

    /**
     * Thread name prefix for per-directory watch threads.
     */
    private static final String WATCH_THREAD = "config-watcher";
    /**
     * Thread name for fixed-rate polling tasks.
     */
    private static final String POLLING_THREAD = "config-watcher-polling";
    /**
     * PropertySource name pattern (Spring Boot 2.4+) for file-based config resources.
     */
    private static final String FILE_SOURCE_CONFIGURATION_PATTERN = "^.*Config\\sresource.*file.*$";
    /**
     * PropertySource name pattern for legacy Spring Boot versions.
     */
    private static final String FILE_SOURCE_CONFIGURATION_PATTERN_LEGACY = "^.+Config:\\s\\[file:.*$";
    /**
     * In-memory registry of property sources that are eligible for reload, keyed by normalized path.
     */
    private static final Map<String, PropertySourceMeta> PROPERTY_SOURCE_META_MAP = new HashMap<>(8);

    /**
     * Spring environment used to access and replace property sources.
     */
    private final StandardEnvironment env;
    /**
     * Publisher used to emit {@link ConfigurationChangedEvent}.
     */
    private final ApplicationEventPublisher eventPublisher;
    /**
     * All available {@link PropertySourceLoader} implementations discovered from Spring factories.
     */
    private final List<PropertySourceLoader> propertyLoaders;

    /**
     * All watch services created by this watcher, closed on bean destroy.
     */
    private final List<WatchService> watchServices = new ArrayList<>(2);
    /**
     * Last observed modified time of the Kubernetes ConfigMap/Secret symbolic link directory.
     */
    private long symbolicLinkModifiedTime = 0;

    /**
     * Create a watcher that can reload external config files and publish change events.
     *
     * @param env            standard environment
     * @param eventPublisher application event publisher
     */
    DynamicConfigPropertiesWatcher(StandardEnvironment env, ApplicationEventPublisher eventPublisher) {
        this.env = env;
        this.eventPublisher = eventPublisher;
        this.propertyLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }

    /**
     * Close all underlying watchers and release resources.
     */
    @Override
    public void destroy() {
        closeConfigDirectoryWatch();
    }

    /**
     * Watch config directory after initializing, using WatchService API
     */
//    @PostConstruct
    @SuppressWarnings("AlibabaThreadPoolCreation")
    public void watchConfigDirectory() {
        MutablePropertySources propertySources = env.getPropertySources();

        for (PropertySource<?> ps : propertySources) {
            boolean isFilePropSource = ps.getName().matches(FILE_SOURCE_CONFIGURATION_PATTERN_LEGACY) || ps.getName().matches(FILE_SOURCE_CONFIGURATION_PATTERN);
            if (isFilePropSource)
                normalizeAndRecordPropSource(ps);
        }

        if (WATCHABLE_TARGETS.size() > 32)
            log.error("too many watch targets of dynamic config, skipped.");
        else {
            int counter = 0;

            for (FileSystemWatchTarget target : WATCHABLE_TARGETS.values()) {
                int threadId = counter;
                Executors.newSingleThreadExecutor(r -> new Thread(r, WATCH_THREAD + "-" + threadId)).submit(() -> this.startWatchDir(target));
                counter++;
            }
        }
    }

    /**
     * Parse the config file path from a property source name and record it for later reload.
     *
     * @param ps property source discovered from the environment
     */
    private void normalizeAndRecordPropSource(PropertySource<?> ps) {
        String name = ps.getName();
        int beginIndex = name.indexOf("[") + 1;
        int endIndex = name.indexOf("]");

        if (beginIndex < 1 && endIndex < 1)
            log.warn("unrecognized config location, property source name is: {}", name);

        String pathStr = name.substring(beginIndex, endIndex);

        if (pathStr.contains(FILE_COLON_SYMBOL))
            pathStr = pathStr.replace(FILE_COLON_SYMBOL, "");

        PROPERTY_SOURCE_META_MAP.put(ConfigurationUtils.trimRelativePathAndReplaceBackSlash(pathStr), new PropertySourceMeta(ps, Paths.get(pathStr), 0L));
        log.debug("configuration file found: {}", pathStr);
    }

    /**
     * Start watching a directory using {@link WatchService} and trigger reloads on file changes.
     *
     * @param target watch target definition
     */
    @SuppressWarnings("BusyWait")
    private void startWatchDir(FileSystemWatchTarget target) {
        try {
            String configLocation = target.getNormalizedDir();
            List<String> filterFiles = target.getFilterFiles();
            log.info("start watching configuration directory: {}", configLocation);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            watchServices.add(watchService);
            Paths.get(configLocation).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            checkChangesWithPeriod();
            WatchKey key;

            while ((key = watchService.take()) != null) {
                // avoid receiving two ENTRY_MODIFY events: file modified and timestamp updated
                Thread.sleep(50);

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path path = (Path) event.context();
                    String confPath = path.toString();

                    if (filterFiles == null)
                        reloadChangedFile(target, confPath, false);
                    else {
                        if (filterFiles.contains(confPath))
                            reloadChangedFile(target, confPath, false);
                        else
                            log.debug("changed path {} is not watched file, skipped.", confPath);
                    }
                }

                key.reset();
            }
            log.warn("config directory watch stopped unexpectedly, dynamic configuration won't take effect.");
        } catch (ClosedWatchServiceException cse) {
            log.info("configuration watcher has been stopped.");
        } catch (Exception ex) {
            log.error("failed to watch config directory: ", ex);
        }
    }


    /**
     * Initialize periodic checks for each watch target (symbolic link polling or full directory scanning).
     *
     * @throws IOException when retrieving file metadata fails
     */
    private void checkChangesWithPeriod() throws IOException {
        for (FileSystemWatchTarget target : WATCHABLE_TARGETS.values()) {
            String configLocation = target.getNormalizedDir();
            Path symLinkPath = Paths.get(configLocation, HIDDEN_SYMBOL_LINK_DIR);
            boolean hasDotDataLinkFile = new File(configLocation, HIDDEN_SYMBOL_LINK_DIR).exists();

            if (hasDotDataLinkFile) {
                log.info("ConfigMap/Secret mode detected, will polling symbolic link instead.");
                symbolicLinkModifiedTime = Files.getLastModifiedTime(symLinkPath, LinkOption.NOFOLLOW_LINKS).toMillis();
                startFixedRateCheckThread(() -> checkSymbolicLink(target), SYMBOL_LINK_POLLING_INTERVAL);
            } else
                // longer check for all config files, make up mechanism if WatchService doesn't work
                startFixedRateCheckThread(() -> reloadAllConfigFiles(target), NORMAL_FILE_POLLING_INTERVAL);
        }
    }

    /**
     * Start a scheduled polling task with fixed delay.
     *
     * @param cmd      task to execute
     * @param interval delay (milliseconds) between executions
     */
    @SuppressWarnings("AlibabaThreadPoolCreation")
    private void startFixedRateCheckThread(Runnable cmd, long interval) {
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, POLLING_THREAD))
                .scheduleWithFixedDelay(cmd, interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Poll the Kubernetes ConfigMap/Secret symbolic link directory and reload on changes.
     *
     * @param target watch target definition
     */
    private void checkSymbolicLink(FileSystemWatchTarget target) {
        try {
            Path symLinkPath = Paths.get(target.getNormalizedDir(), HIDDEN_SYMBOL_LINK_DIR);
            long tmp = Files.getLastModifiedTime(symLinkPath, LinkOption.NOFOLLOW_LINKS).toMillis();

            if (tmp != symbolicLinkModifiedTime) {
                reloadAllConfigFiles(target, true);
                symbolicLinkModifiedTime = tmp;
            }
        } catch (IOException ex) {
            log.warn("could not check symbolic link of config dir: {}", ex.getMessage());
        }
    }

    /**
     * Reload all configuration files for the given target.
     *
     * @param target watch target definition
     */
    private void reloadAllConfigFiles(FileSystemWatchTarget target) {
        reloadAllConfigFiles(target, false);
    }

    /**
     * Reload all configuration files for the given target, optionally forcing reload even if timestamp is unchanged.
     *
     * @param target      watch target definition
     * @param forceReload whether to force reload
     */
    private void reloadAllConfigFiles(FileSystemWatchTarget target, boolean forceReload) {
        try (Stream<Path> paths = Files.walk(Paths.get(target.getNormalizedDir()))) {
            paths.filter(path -> !Files.isDirectory(path)).forEach((path) -> {
                String rawPath = path.toString();

                if (target.getFilterFiles() != null) {
                    if (target.getFilterFiles().contains(rawPath))
                        reloadChangedFile(target, rawPath, forceReload);
                } else
                    reloadChangedFile(target, rawPath, forceReload);
            });
        } catch (IOException e) {
            log.warn("can not walk through config directory: {}", e.getMessage());
        }
    }

    /**
     * Reload a changed file if it matches the watched property source and has a new modified timestamp.
     *
     * @param target      watch target definition
     * @param rawPath     changed path (may be relative to the watched directory)
     * @param forceReload whether to force reload regardless of timestamps
     */
    private void reloadChangedFile(FileSystemWatchTarget target, String rawPath, boolean forceReload) {
        String fullPathStr = ConfigurationUtils.normalizePath(rawPath, target.getNormalizedDir());
        Path path = Paths.get(fullPathStr);

        if (HIDDEN_SYMBOL_LINK_DIR.equals(path.getFileName().toString()))
            return;

        try {
            PropertySourceMeta propertySourceMeta = PROPERTY_SOURCE_META_MAP.get(fullPathStr);

            if (propertySourceMeta == null) {
                // also try abs path, in case of the configTree case
                String absolutePath = ConfigurationUtils.trimRelativePathAndReplaceBackSlash(new File(fullPathStr).getAbsolutePath());
                propertySourceMeta = PROPERTY_SOURCE_META_MAP.get(absolutePath);

                if (propertySourceMeta == null) {
                    log.debug("changed file at config location is not recognized: {}", fullPathStr);
                    return;
                }
            }

            long currentModTs = Files.getLastModifiedTime(path).toMillis();
            long mdt = propertySourceMeta.getLastModifyTime();

            if (forceReload || mdt != currentModTs)
                doReloadConfigFile(target, propertySourceMeta, fullPathStr, currentModTs);
        } catch (Exception ex) {
            log.error("reload configuration file {} failed: ", fullPathStr, ex);
        }
    }

    /**
     * Reload a config file using a matching {@link PropertySourceLoader}.
     *
     * @param target           watch target definition
     * @param propertySourceMeta matched property source metadata
     * @param path             normalized config file path
     * @param modifyTime       last modified timestamp of the file
     * @throws IOException when loading file fails
     */
    private void doReloadConfigFile(FileSystemWatchTarget target, PropertySourceMeta propertySourceMeta, String path, long modifyTime) throws IOException {
        log.info("dynamic config file has been changed: {}", path);
        String extension = ConfigurationUtils.getFileExtension(path);

        for (PropertySourceLoader loader : propertyLoaders) {
            if (Arrays.asList(loader.getFileExtensions()).contains(extension)) {
                // use this loader to load config resource
                loadPropertiesAndPublishEvent(target, propertySourceMeta, loader, path, modifyTime);
                break;
            }
        }
    }

    /**
     * Load a changed config file, compute the diff, replace the property source and publish an event.
     *
     * @param target           watch target definition
     * @param propertySourceMeta matched property source metadata
     * @param loader           property source loader
     * @param path             normalized config file path
     * @param modifyTime       last modified timestamp of the file
     * @throws IOException when loading file fails
     */
    @SuppressWarnings("unchecked")
    private void loadPropertiesAndPublishEvent(FileSystemWatchTarget target, PropertySourceMeta propertySourceMeta, PropertySourceLoader loader, String path, long modifyTime) throws IOException {
        FileSystemResource resource = new FileSystemResource(path);
        String propertySourceName = propertySourceMeta.getPropertySource().getName();
        List<PropertySource<?>> newPropsList = loader.load(propertySourceName, resource);

        if (newPropsList.isEmpty()) {
            log.warn("properties not loaded after config changed: {}", path);
            return;
        }

        PropertySource<?> previous = env.getPropertySources().get(propertySourceName);
        PropertySource<?> newProps = newPropsList.get(0);

        if (previous == null) {
            log.warn("previous property source can not be found, skipped.");
            return;
        }

        if (target.getType() == WatchTargetType.CONFIG_IMPORT_TREE) {
            // need add the key prefix back
            String prefix = ConfigurationUtils.getPropertyPrefix(target.getRootDir(), Paths.get(resource.getPath()));
            newProps = ConfigurationUtils.addConfigPropPrefix((OriginTrackedMapPropertySource) newPropsList.get(0), prefix);
        }

        Map<String, Object> diff = ConfigurationChangedEvent.getPropertyDiff(
                (Map<Object, OriginTrackedValue>) previous.getSource(),
                (Map<Object, OriginTrackedValue>) newProps.getSource()
        );

        if (diff.isEmpty()) {
            log.info("config file has been changed but no actual value changed, dynamic config event skipped.");
            return;
        }

        ConfigurationChangedEvent event = new ConfigurationChangedEvent(path, previous, newProps, diff);
        env.getPropertySources().replace(propertySourceName, newProps);
        propertySourceMeta.setLastModifyTime(modifyTime);
        eventPublisher.publishEvent(event);
    }

    /**
     * Close all {@link WatchService} instances started by this watcher.
     */
    private void closeConfigDirectoryWatch() {
        if (!watchServices.isEmpty()) {
            try {
                for (WatchService w : watchServices)
                    w.close();

                log.info("config properties watcher bean is destroying, WatchService stopped.");
            } catch (IOException e) {
                log.warn("can not close config directory watcher. ", e);
            }
        }
    }
}
