package com.ajaxjs.monitor.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InstanceEventLog {
	private List<InstanceEvent> eventList = new ArrayList<>();

	private boolean hasInit = false;

	public void add(InstanceEvent instanceEvent) {
		eventList.add(instanceEvent);
	}
}
