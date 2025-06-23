package com.ajaxjs.api.security.referer;

import com.ajaxjs.api.InterceptorAction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Simply check the HTTP Referer
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpReferer extends InterceptorAction<HttpRefererCheck> {
    /**
     * Add your domains here to open
     */
    protected List<String> ALLOWED_REFERRERS;

    @Override
    public boolean action(HttpRefererCheck annotation, HttpServletRequest req) {
        String referer = req.getHeader("Referer");  // 获取 Referer 头

        if (ALLOWED_REFERRERS != null && (!StringUtils.hasText(referer) || !ALLOWED_REFERRERS.contains(referer)))
            throw new SecurityException("Invalid Referer header.");

        return true;
    }
}
