package com.ajaxjs.monitor.event;

import com.ajaxjs.monitor.model.Registration;

import java.time.Instant;

/**
 * This event gets emitted when an instance is registered.
 *
 * @author Johannes Edmeier
 */
//@Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceRegisteredEvent extends InstanceEvent {
	private static final long serialVersionUID = 1L;
	private final Registration registration;

	public InstanceRegisteredEvent(String instance, long version, Registration registration) {
		this(instance, version, Instant.now(), registration);
	}

	public InstanceRegisteredEvent(String instance, long version, Instant timestamp, Registration registration) {
		super(instance, version, "REGISTERED", timestamp);
		this.registration = registration;
	}
}
