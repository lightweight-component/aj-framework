package com.ajaxjs.oauth.utils;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.config.AuthSource;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.auth.core.AuthScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StrUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 授权配置类的校验器
 */
public class AuthChecker {
    /**
     * 是否支持第三方登录
     *
     * @param config config
     * @param source source
     * @return true or false
     * @since 1.6.1-beta
     */
    public static boolean isSupportedAuth(AuthConfig config, AuthSource source) {
        boolean isSupported = StrUtil.hasText(config.getClientId()) && StrUtil.hasText(config.getClientSecret());
        if (isSupported && AuthDefaultSource.STACK_OVERFLOW == source)
            isSupported = StrUtil.hasText(config.getStackOverflowKey());

        if (isSupported && AuthDefaultSource.WECHAT_ENTERPRISE == source)
            isSupported = StrUtil.hasText(config.getAgentId());

        if (isSupported && (AuthDefaultSource.CODING == source || AuthDefaultSource.OKTA == source))
            isSupported = StrUtil.hasText(config.getDomainPrefix());

        if (isSupported && AuthDefaultSource.XMLY == source)
            isSupported = StrUtil.hasText(config.getDeviceId()) && null != config.getClientOsType();

        if (isSupported)
            isSupported = config.getClientOsType() == 3 || StrUtil.hasText(config.getPackId());

        return isSupported;
    }

    /**
     * 检查配置合法性。针对部分平台， 对redirect uri有特定要求。一般来说redirect uri都是http://，而对于facebook平台， redirect uri 必须是https的链接
     *
     * @param config config
     * @param source source
     */
    public static void checkConfig(AuthConfig config, AuthSource source) {
        String redirectUri = config.getRedirectUri();
        if (config.isIgnoreCheckRedirectUri())
            return;

        if (StrUtil.isEmptyText(redirectUri))
            throw new AuthException(ResponseStatus.ILLEGAL_REDIRECT_URI, source);

        if (!GlobalAuthUtils.isHttpProtocol(redirectUri) && !GlobalAuthUtils.isHttpsProtocol(redirectUri))
            throw new AuthException(ResponseStatus.ILLEGAL_REDIRECT_URI, source);
    }

    /**
     * 校验回调传回的code
     * <p>
     * {@code v1.10.0}版本中改为传入{@code source}和{@code callback}，对于不同平台使用不同参数接受code的情况统一做处理
     *
     * @param source   当前授权平台
     * @param callback 从第三方授权回调回来时传入的参数集合
     * @since 1.8.0
     */
    public static void checkCode(AuthSource source, AuthCallback callback) {
        // 推特平台不支持回调 code 和 state
        if (source == AuthDefaultSource.TWITTER)
            return;

        String code = callback.getCode();

        if (StrUtil.isEmptyText(code) && source == AuthDefaultSource.HUAWEI)
            code = callback.getAuthorization_code();

        if (StrUtil.isEmptyText(code))
            throw new AuthException(ResponseStatus.ILLEGAL_CODE, source);
    }

    /**
     * 校验回调传回的{@code state}，为空或者不存在
     * <p>
     * {@code state}不存在的情况只有两种：
     * 1. {@code state}已使用，被正常清除
     * 2. {@code state}为前端伪造，本身就不存在
     *
     * @param state          {@code state}一定不为空
     * @param source         {@code source}当前授权平台
     * @param authStateCache {@code authStateCache} state缓存实现
     */
    public static void checkState(String state, AuthSource source, AuthStateCache authStateCache) {
        // 推特平台不支持回调 code 和 state
        if (source == AuthDefaultSource.TWITTER)
            return;

        if (StrUtil.isEmptyText(state) || !authStateCache.containsKey(state))
            throw new AuthException(ResponseStatus.ILLEGAL_STATUS, source);
    }

    /**
     * 从 {@link AuthScope} 数组中获取实际的 scope 字符串
     *
     * @param scopes 可变参数，支持传任意 {@link AuthScope}
     * @return List
     */
    public static List<String> getScopes(AuthScope... scopes) {
        if (null == scopes || scopes.length == 0)
            return null;

        return Arrays.stream(scopes).map(AuthScope::getScope).collect(Collectors.toList());
    }

    /**
     * 获取 {@link AuthScope} 数组中所有的被标记为 {@code default} 的 scope
     *
     * @param scopes scopes
     * @return List
     */
    public static List<String> getDefaultScopes(AuthScope[] scopes) {
        if (null == scopes || scopes.length == 0)
            return null;

        return Arrays.stream(scopes).filter((AuthScope::isDefault)).map(AuthScope::getScope).collect(Collectors.toList());
    }

    /* 该配置仅用于支持 PKCE 模式的平台，针对无服务应用，不推荐使用隐式授权，推荐使用 PKCE 模式 */
    public static String generateCodeVerifier() {
        String randomStr = RandomTools.generateRandomString(50);
        return Base64Utils.encodeUrlSafe(randomStr);
    }

    /**
     * 适用于 OAuth 2.0 PKCE 增强协议
     *
     * @param codeChallengeMethod s256 / plain
     * @param codeVerifier        客户端生产的校验码
     * @return code challenge
     */
    public static String generateCodeChallenge(String codeChallengeMethod, String codeVerifier) {
        if ("S256".equalsIgnoreCase(codeChallengeMethod))
            // https://tools.ietf.org/html/rfc7636#section-4.2
            // code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
            return new String(Objects.requireNonNull(Base64Utils.encodeUrlSafe(Sha256.digest(codeVerifier), true)), StandardCharsets.US_ASCII);
        else
            return codeVerifier;
    }
}
