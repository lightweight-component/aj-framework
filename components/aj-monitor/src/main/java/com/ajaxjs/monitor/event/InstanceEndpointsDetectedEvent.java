package com.ajaxjs.monitor.event;

import com.ajaxjs.monitor.model.Endpoints;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;

/**
 * This event gets emitted when all instance's endpoints are discovered.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@Data
public class
InstanceEndpointsDetectedEvent extends InstanceEvent {
	private static final long serialVersionUID = 1L;
	private final Endpoints endpoints;

	public InstanceEndpointsDetectedEvent(String instance, long version, Endpoints endpoints) {
		this(instance, version, Instant.now(), endpoints);
	}

	public InstanceEndpointsDetectedEvent(String instance, long version, Instant timestamp, Endpoints endpoints) {
		super(instance, version, "ENDPOINTS_DETECTED", timestamp);
		this.endpoints = endpoints;
	}
}
