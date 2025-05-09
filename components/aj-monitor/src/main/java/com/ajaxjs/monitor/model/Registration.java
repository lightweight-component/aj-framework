package com.ajaxjs.monitor.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Data
@ToString(exclude = "metadata")
public class Registration implements Serializable {
	private static final long serialVersionUID = -2685324097568177886L;
	private final String name;
    @Nullable
    private final String managementUrl;
    private final String healthUrl;
    @Nullable
    private final String serviceUrl;
    private final String source;
    private final Map<String, String> metadata;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
	public Registration(String name, @Nullable String managementUrl, String healthUrl,
                         @Nullable String serviceUrl, String source) {
         Objects.requireNonNull(name, "'name' must not be empty.");
         Objects.requireNonNull(healthUrl, "'healthUrl' must not be empty.");
        Assert.isTrue(checkUrl(healthUrl), "'healthUrl' is not valid: " + healthUrl);
        Assert.isTrue(StrUtil.isEmptyTextText(managementUrl) || checkUrl(managementUrl),
            "'managementUrl' is not valid: " + managementUrl
        );
        Assert.isTrue(StrUtil.isEmptyTextText(serviceUrl) || checkUrl(serviceUrl),
            "'serviceUrl' is not valid: " + serviceUrl
        );
        this.name = name;
        this.managementUrl = managementUrl;
        this.healthUrl = healthUrl;
        this.serviceUrl = serviceUrl;
        this.source = source;
        this.metadata = new LinkedHashMap<>();
        metadata.put("startup", Instant.now().toString());
    }

    public static Builder create(String name, String healthUrl) {
        return builder().name(name).healthUrl(healthUrl);
    }

    public static Builder copyOf(Registration registration) {
        return registration.toBuilder();
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Checks the syntax of the given URL.
     *
     * @param url The URL.
     * @return true, if valid.
     */
    private boolean checkUrl(String url) {
        try {
            return new URI(url).isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
