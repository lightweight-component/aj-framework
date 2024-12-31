package com.ajaxjs.monitor.model;

import java.io.Serializable;

import lombok.Data;
import org.springframework.util.Assert;

@Data
public class Endpoint implements Serializable {
	private static final long serialVersionUID = -8656237790482826441L;
	public static final String INFO = "info";
    public static final String HEALTH = "health";
    public static final String LOGFILE = "logfile";
    public static final String ENV = "env";
    public static final String HTTPTRACE = "httptrace";
    public static final String THREADDUMP = "threaddump";
    public static final String LIQUIBASE = "liquibase";
    public static final String FLYWAY = "flyway";
    public static final String ACTUATOR_INDEX = "actuator-index";
    private final String id;
    private final String url;

    Endpoint(String id, String url) {
        Assert.hasText(id, "'id' must not be empty.");
        Assert.hasText(url, "'url' must not be empty.");
        this.id = id;
        this.url = url;
    }
}
