package com.ajaxjs.framework.dynamicconfig.model;

import com.ajaxjs.framework.dynamicconfig.DynamicConfigPropertiesWatcher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Application event that represents configuration file has been changed
 *
 * @author Code2Life
 * @see DynamicConfigPropertiesWatcher
 */
@Getter
@Setter
@Slf4j
public class ConfigurationChangedEvent extends ApplicationEvent {
    /**
     * Path of the file that changed and triggered this event
     */
    private String path;

    /**
     * previous property source of changed config file
     */
    private PropertySource<?> previous;

    /**
     * current property source of changed config file
     */
    private PropertySource<?> current;

    /**
     * The diff properties, keys are normalized, values are newest values, null means the value deleted
     */
    private Map<String, Object> diff;

    /**
     * Create an event with property-source snapshots and their diff.
     *
     * @param path     path of the changed configuration file
     * @param previous previous property source
     * @param current  current property source
     * @param diff     diff map, where {@code null} value indicates key removal
     */
    public ConfigurationChangedEvent(String path, PropertySource<?> previous, PropertySource<?> current, Map<String, Object> diff) {
        super(path);
        this.path = path;
        this.previous = previous;
        this.current = current;
        this.diff = diff;
    }

    /**
     * loop current properties and prev properties, find diff
     * removed properties won't impact existing bean values
     */
    public static Map<String, Object> getPropertyDiff(Map<Object, OriginTrackedValue> prev, Map<Object, OriginTrackedValue> current) {
        Map<String, Object> diff = new HashMap<>(4);
        filterAddOrUpdatedKeys(prev, current, diff);
        filterMissingKeys(prev, current, diff);

        return diff;
    }

    /**
     * Collect added or updated keys into {@code diff}.
     *
     * @param prev    previous property map
     * @param current current property map
     * @param diff    output diff map
     */
    private static void filterAddOrUpdatedKeys(Map<Object, OriginTrackedValue> prev, Map<Object, OriginTrackedValue> current, Map<String, Object> diff) {
        for (Map.Entry<Object, OriginTrackedValue> entry : current.entrySet()) {
            Object k = entry.getKey();
            OriginTrackedValue v = entry.getValue();

            if (prev.containsKey(k)) {
                if (!Objects.equals(v, prev.get(k))) {
                    diff.put(k.toString(), v.getValue());
                    log.debug("found changed key of dynamic config: {}", k);
                }
            } else {
                diff.put(k.toString(), v.getValue());
                log.debug("found new added key of dynamic config: {}", k);
            }
        }
    }

    /**
     * Collect removed keys into {@code diff} with {@code null} values.
     *
     * @param prev    previous property map
     * @param current current property map
     * @param diff    output diff map
     */
    private static void filterMissingKeys(Map<Object, OriginTrackedValue> prev, Map<Object, OriginTrackedValue> current, Map<String, Object> diff) {
        for (Map.Entry<Object, OriginTrackedValue> entry : prev.entrySet()) {
            Object k = entry.getKey();

            if (!current.containsKey(k)) {
                diff.put(k.toString(), null);
                log.debug("found deleted k of dynamic config: {}", k);
            }
        }
    }
}
