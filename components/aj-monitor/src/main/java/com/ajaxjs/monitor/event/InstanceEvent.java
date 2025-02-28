package com.ajaxjs.monitor.event;


import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * Abstract Event regarding registered instances
 *
 * @author Johannes Edmeier
 */
@Data
public abstract class InstanceEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String instance;
    private final long version;
    private final Instant timestamp;
    private final String type;

    protected InstanceEvent(String instance, long version, String type, Instant timestamp) {
        this.instance = instance;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
    }
}
