package com.ajaxjs.framework.wechat.merchant;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import com.ajaxjs.util.io.Resources;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;

/**
 * 签名生成器
 */
@Slf4j
public class SignerMaker {
    private final MerchantConfig cfg;

    public final PrivateKey privateKey;

    /**
     * 创建签名生成器
     *
     * @param cfg 商户平台的配置
     */
    public SignerMaker(MerchantConfig cfg) {
        this.cfg = cfg;
        this.privateKey = loadPrivateKeyByPath(cfg.getPrivateKey());
    }

    private static String privateKeyContent;

    /**
     * 从 classpath 上指定私钥文件的路径
     *
     * @param privateKeyPath 私钥文件的路径
     * @return 私钥文件 PrivateKey
     */
    public static PrivateKey loadPrivateKeyByPath(String privateKeyPath) {
        if (privateKeyContent == null)
            privateKeyContent = Resources.getResourceText(privateKeyPath); // cache it

        if (ObjectHelper.isEmptyText(privateKeyContent))
            throw new BusinessException("证书为空，请检查路径是否正确");

        return KeyMgr.restorePrivateKey(privateKeyContent);
    }

    /**
     * 生成签名
     *
     * @param request 请求信息
     * @return 签名 Token
     */
    public String getToken(HttpRequestWrapper request) {
        String nonceStr = RandomTools.generateRandomString(32);
        long timestamp = System.currentTimeMillis() / 1000;
        String message = buildMessage(request, nonceStr, timestamp);
        log.debug("authorization message=[{}]", message);
        String signature = new DoSignature(Constant.SHA256_RSA).setPrivateKey(privateKey).setStrData(message).signToString();

        // @formatter:off
        String token = "mchid=\"" + cfg.getMchId() + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + cfg.getMchSerialNo() + "\","
                + "signature=\"" + signature + "\"";
        // @formatter:on

        log.debug("authorization token=[{}]", token);

        return token;
    }

    /**
     * 生成签名明文
     *
     * @param request   请求信息
     * @param nonceStr  随机字符串
     * @param timestamp 时间戳
     * @return 签名明文
     */
    private static String buildMessage(HttpRequestWrapper request, String nonceStr, long timestamp) {
        // @formatter:off
        return request.method + "\n"
            + request.url + "\n"
            + timestamp + "\n"
            + nonceStr + "\n"
            + request.body + "\n";
        // @formatter:on
    }
}
