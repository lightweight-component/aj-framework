package com.ajaxjs.security.antireptile.util;

import com.ajaxjs.security.antireptile.model.VerifyImageDTO;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * 验证码图片生成及本地缓存答案存取工具。
 * <p>每次生成独立 UUID 与 Base64 图片，调用方需要显式保存答案。
 * 本地缓存答案有效期固定为 60 秒；读取不删除答案，刷新时由调用方主动删除旧答案。</p>
 */
public class VerifyImageUtil {
    /**
     * 验证码答案缓存键前缀，后拼接 UUID；保留现有键名以兼容已存储数据。
     */
    private static final String VERIFY_CODE_KEY = "kk-antireptile_verifycdoe_";

    /**
     * kaptcha 文字及图片生成器，可由宿主应用提供自定义 Producer Bean。
     */
    @Autowired
    private Producer captchaProducer;

    /**
     * 当前实例私有的验证码答案缓存。
     */
    @Autowired
    private LocalCache localCache;

    /**
     * 生成验证码图片、正确答案及独立 UUID，不自动写入本地缓存。
     * <p>图片以带 {@code data:image/png;base64,} 前缀的字符串返回，
     * 默认生成五位静态文字验证码，答案按固定语言环境转为小写，保持大小写不敏感。
     * 验证码类型当前设为 {@code null}。需另行调用保存方法建立答案缓存。</p>
     *
     * @return 包含标识、PNG 图片和小写正确答案的内部对象
     * @throws IllegalStateException PNG 编码器不可用或图片编码失败时抛出
     */
    public VerifyImageDTO generateVerifyImg() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String text = captchaProducer.createText();

        try {
            if (!ImageIO.write(captchaProducer.createImage(text), "png", outputStream)) {
                throw new IllegalStateException("PNG image writer is not available");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode captcha image", e);
        }

        String result = text.toLowerCase(Locale.ROOT);
        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        String verifyId = UUID.randomUUID().toString();

        return new VerifyImageDTO(verifyId, null, base64Image, result);
    }

    /**
     * 按验证码标识保存正确答案，并设置 60 秒有效期。
     *
     * @param verifyImage 已生成且含标识与答案的验证码对象
     */
    public void saveVerifyCode(VerifyImageDTO verifyImage) {
        localCache.put(VERIFY_CODE_KEY + verifyImage.getVerifyId(), verifyImage.getResult(), 60_000L);
    }

    /**
     * 删除指定标识的答案缓存，缓存不存在时无需额外处理。
     *
     * @param verifyId 待失效的验证码标识
     */
    public void deleteVerifyCode(String verifyId) {
        localCache.remove(VERIFY_CODE_KEY + verifyId);
    }

    /**
     * 读取指定标识对应的正确答案，不删除缓存或延长有效期。
     *
     * @param verifyId 待查询的验证码标识
     * @return 缓存答案；不存在或已过期时为 {@code null}
     */
    public String getVerifyCode(String verifyId) {
        return localCache.get(VERIFY_CODE_KEY + verifyId, String.class);
    }
}
