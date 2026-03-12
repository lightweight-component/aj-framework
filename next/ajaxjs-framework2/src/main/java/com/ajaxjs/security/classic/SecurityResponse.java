/**
 * Copyright 2015 Sp42 frank@ajaxjs.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.security.classic;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 *
 */
public class SecurityResponse extends HttpServletResponseWrapper {
    public boolean isXssCheck;

    public boolean isCRLFCheck;

    /**
     * 是否检测 cookie 大小
     */
    public boolean isCookiesSizeCheck;

    /**
     * 单个 cookie 最大大小，单位：kb
     * 如果放置 JWT 应该不超过 1kb
     */
    public int maxCookieSize = 2;

    /**
     * 创建一个 SecurityResponse 实例。
     *
     * @param response 响应对象
     */
    public SecurityResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (isCRLFCheck) {
            String name = cookie.getName(), value = cookie.getValue();
            Cookie newCookie = new Cookie(InstallFilter.cleanCRLF(name), InstallFilter.cleanCRLF(value));

            if (cookie.getDomain() != null)
                newCookie.setDomain(cookie.getDomain());

//            newCookie.setComment(cookie.getComment());
            newCookie.setHttpOnly(cookie.isHttpOnly());
            newCookie.setMaxAge(cookie.getMaxAge());
            newCookie.setPath(cookie.getPath());
            newCookie.setSecure(cookie.getSecure());
//            newCookie.setVersion(cookie.getVersion());

            /*
             * 检查 Cookie 容量大小和是否在白名单中。
             */
            if (isCookiesSizeCheck && (cookie.getValue().length() > maxCookieSize * 1024))
                throw new SecurityException("超出 Cookie 允许容量：" + maxCookieSize * 1024);

//		if (!delegate.isInWhiteList(cookie.getName()))
//			throw new SecurityException("cookie: " + cookie.getName() + " 不在白名单中，添加无效！");

            super.addCookie(newCookie);
        } else
            super.addCookie(cookie);
    }

    @Override
    public void setDateHeader(String name, long date) {
        if (isCRLFCheck)
            name = InstallFilter.cleanCRLF(name);

        super.setDateHeader(name, date);
    }

    @Override
    public void setIntHeader(String name, int value) {
        if (isCRLFCheck)
            name = InstallFilter.cleanCRLF(name);

        super.setIntHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (isXssCheck)
            value = InstallFilter.cleanXSS(value);

        if (isCRLFCheck) {
            name = InstallFilter.cleanCRLF(name);
            value = InstallFilter.cleanCRLF(value);
        }

        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (isXssCheck)
            value = InstallFilter.cleanXSS(value);

        if (isCRLFCheck) {
            name = InstallFilter.cleanCRLF(name);
            value = InstallFilter.cleanCRLF(value);
        }

        super.setHeader(name, value);
    }

    @Override
    public void sendError(int sc, String value) throws IOException {
        if (isXssCheck)
            value = InstallFilter.cleanCRLF(value);

        super.sendError(sc, value);
    }
}
