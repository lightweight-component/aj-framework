package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.enums.scope.AuthAppleScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.AuthChecker;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.security.AbstractJwk;
import lombok.Data;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthAppleRequest extends AuthDefaultRequest {

    private static final String AUD = "https://appleid.apple.com";

    private volatile PrivateKey privateKey;

    public AuthAppleRequest(AuthConfig config) {
        super(config, AuthDefaultSource.APPLE);
    }

    public AuthAppleRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.APPLE, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("response_mode", "form_post")
            .queryParam("scope", this.getScopes(" ", false, AuthChecker.getDefaultScopes(AuthAppleScope.values())))
            .build();
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        if (!StrUtil.isEmptyText(authCallback.getError())) {
            throw new AuthException(authCallback.getError());
        }
        this.config.setClientSecret(this.getToken());
        // if failed will throw AuthException
        String response = doPostAuthorizationCode(authCallback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        // https://developer.apple.com/documentation/sign_in_with_apple/tokenresponse
        AuthToken.AuthTokenBuilder builder = AuthToken.builder()
            .accessToken(accessTokenObject.getString("access_token"))
            .expireIn(accessTokenObject.getIntValue("expires_in"))
            .refreshToken(accessTokenObject.getString("refresh_token"))
            .tokenType(accessTokenObject.getString("token_type"))
            .idToken(accessTokenObject.getString("id_token"));
        if (!StrUtil.isEmptyText(authCallback.getUser())) {
            try {
                AppleUserInfo userInfo = JSONObject.parseObject(authCallback.getUser(), AppleUserInfo.class);
                builder.username(userInfo.getName().getFirstName() + " " + userInfo.getName().getLastName());
            } catch (Exception ignored) {
            }
        }
        return builder.build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String[] idToken = authToken.getIdToken().split("\\.");
        String payload = new String(urlDecoder.decode(idToken[1]));
        JSONObject object = JSONObject.parseObject(payload);
        // https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/authenticating_users_with_sign_in_with_apple#3383773
        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("sub"))
            .email(object.getString("email"))
            .username(authToken.getUsername())
            .token(authToken)
            .source(source.toString())
            .build();
    }

    @Override
    protected void checkConfig(AuthConfig config) {
        super.checkConfig(config);
        if (StrUtil.isEmptyText(config.getClientId())) {
            throw new AuthException(ResponseStatus.ILLEGAL_CLIENT_ID, source);
        }
        if (StrUtil.isEmptyText(config.getClientSecret())) {
            throw new AuthException(ResponseStatus.ILLEGAL_CLIENT_SECRET, source);
        }
        if (StrUtil.isEmptyText(config.getKid())) {
            throw new AuthException(ResponseStatus.ILLEGAL_KID, source);
        }
        if (StrUtil.isEmptyText(config.getTeamId())) {
            throw new AuthException(ResponseStatus.ILLEGAL_TEAM_ID, source);
        }
    }

    /**
     * 获取token
     * @see <a href="https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret">creating-a-client-secret</a>
     * @return jwt token
     */
    private String getToken() {
        return Jwts.builder().header().add(AbstractJwk.KID.getId(), this.config.getKid()).and()
            .issuer(this.config.getTeamId())
            .subject(this.config.getClientId())
            .audience().add(AUD).and()
            .expiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3)))
            .issuedAt(new Date())
            .signWith(getPrivateKey())
            .compact();
    }

    private PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            synchronized (this) {
                if (this.privateKey == null) {
                    try (PEMParser pemParser = new PEMParser(new StringReader(this.config.getClientSecret()))) {
                        JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter();
                        PrivateKeyInfo keyInfo = (PrivateKeyInfo) pemParser.readObject();
                        this.privateKey = pemKeyConverter.getPrivateKey(keyInfo);
                    } catch (IOException e) {
                        throw new AuthException("Failed to get apple private key", e);
                    }
                }
            }
        }
        return this.privateKey;
    }

    @Data
    static class AppleUserInfo {
        private AppleUsername name;
        private String email;
    }

    @Data
    static class AppleUsername {
        private String firstName;
        private String lastName;
    }
}
