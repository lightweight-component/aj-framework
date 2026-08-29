package com.ajaxjs.security.referer;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.ObjectHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * Simply check the HTTP Referer
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.HttpReferer.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.http-referer")
public class HttpReferer extends InterceptorAction<HttpRefererCheck> {
    /**
     * Add your domains here to open
     */
    private List<String> allowedReferrers;

    /**
     * Executes the action operation.
     *
     * @param annotation the annotation parameter.
     * @param req        the req parameter.
     * @return the operation result.
     */
    @Override
    public boolean action(HttpRefererCheck annotation, HttpServletRequest req) {
        if (!ObjectHelper.isEmpty(allowedReferrers)) {
            String referer = req.getHeader(HttpHeaders.REFERER);  // 获取 Referer 头

            if ((!StringUtils.hasText(referer) || !allowedReferrers.contains(referer)))
                throw new SecurityException("Invalid Referer header.");
        }

        return true;
    }
}
