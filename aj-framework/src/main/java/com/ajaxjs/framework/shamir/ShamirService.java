package com.ajaxjs.framework.shamir;

import com.ajaxjs.framework.shamir.model.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShamirService {
    /**
     * 存储会话信息（演示用，生产环境应使用数据库）
     * key: sessionId, value: 会话元数据
     */
    private final Map<String, SessionMetadata> sessionStore = new ConcurrentHashMap<>();

    /**
     * 拆分密钥
     */
    public SplitResponse split(SplitRequest request) {
        // 参数校验
        if (request.getSecret() == null || request.getSecret().isEmpty())
            throw new IllegalArgumentException("Secret cannot be empty");

        if (request.getTotalShares() == null || request.getTotalShares() < 2)
            throw new IllegalArgumentException("Total shares must be at least 2");

        if (request.getThreshold() == null || request.getThreshold() < 2)
            throw new IllegalArgumentException("Threshold must be at least 2");

        if (request.getThreshold() > request.getTotalShares())
            throw new IllegalArgumentException("Threshold cannot exceed total shares");

        byte[] secretBytes = request.getSecret().getBytes(StandardCharsets.UTF_8); // 将密钥转换为字节数组
        List<Share> shares = split(secretBytes, request.getTotalShares(), request.getThreshold()); // 调用 Shamir 算法拆分
        List<String> encodedShares = shares.stream().map(Share::encode).collect(Collectors.toList()); // 编码份额为字符串
        String sessionId = UUID.randomUUID().toString(); // 生成会话 ID

        // 存储会话元数据（演示用）
        sessionStore.put(sessionId, new SessionMetadata(sessionId, request.getTotalShares(), request.getThreshold()));

        return new SplitResponse(sessionId, encodedShares, String.format("密钥已拆分为 %d 份，任意 %d 份可恢复原始密钥", request.getTotalShares(), request.getThreshold()));
    }

    /**
     * 恢复密钥
     */
    public CombineResponse combine(CombineRequest request) {
        // 参数校验
        if (request.getShares() == null || request.getShares().isEmpty())
            return new CombineResponse(null, "密钥份额列表不能为空", false);

        try {
            List<Share> shares = request.getShares().stream().map(Share::decode).collect(Collectors.toList()); // 解码份额

            byte[] secretBytes = combine(shares);// 调用 Shamir 算法恢复
            String secret = new String(secretBytes, StandardCharsets.UTF_8).trim();// 转换为字符串（处理可能的多余字节）

            if (secret.charAt(0) == '\0') // 移除可能的前导零字节
                secret = secret.substring(1);

            return new CombineResponse(secret, String.format("成功使用 %d 个份额恢复密钥", shares.size()), true);
        } catch (Exception e) {
            return new CombineResponse(null, "恢复失败：" + e.getMessage(), false);
        }
    }

    /**
     * 获取所有会话（演示用）
     */
    public Map<String, SessionMetadata> getAllSessions() {
        return new HashMap<>(sessionStore);
    }

    /**
     * 清理过期会话（演示用，生产环境应使用定时任务）
     */
    public void cleanExpiredSessions(long maxAgeMillis) {
        long now = System.currentTimeMillis();
        sessionStore.entrySet().removeIf(entry -> now - entry.getValue().getCreateTime() > maxAgeMillis);
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    // 使用一个大素数作为有限域的模
    private static final BigInteger PRIME = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);



    /**
     * 拆分密钥
     *
     * @param secret    原始密钥（字节数组）
     * @param n         总份额数
     * @param threshold 门限值（至少需要多少份才能恢复）
     * @return 密钥份额列表
     */
    public static List<Share> split(byte[] secret, int n, int threshold) {
        if (threshold > n)
            throw new IllegalArgumentException("Threshold cannot be greater than total shares");

        if (threshold < 2)
            throw new IllegalArgumentException("Threshold must be at least 2");

        BigInteger secretInt = new BigInteger(1, secret); // 将密钥转换为 BigInteger

        if (secretInt.compareTo(PRIME) >= 0) // 确保密钥小于素数
            throw new IllegalArgumentException("Secret is too large");

        // 生成随机多项式系数：f(x) = a0 + a1*x + a2*x^2 + ... + a(t-1)*x^(t-1)
        // 其中 a0 = secret
        BigInteger[] coefficients = new BigInteger[threshold];
        coefficients[0] = secretInt;

        for (int i = 1; i < threshold; i++)
            coefficients[i] = new BigInteger(PRIME.bitLength(), RANDOM).mod(PRIME);

        List<Share> shares = new ArrayList<>(); // 生成 n 个份额

        for (int x = 1; x <= n; x++) {
            BigInteger y = evaluatePolynomial(coefficients, x);
            shares.add(new Share(x, y));
        }

        return shares;
    }

    /**
     * 恢复密钥
     *
     * @param shares 至少 threshold 个份额
     * @return 原始密钥（字节数组）
     */
    public static byte[] combine(List<Share> shares) {
        if (shares == null || shares.isEmpty())
            throw new IllegalArgumentException("Shares list cannot be empty");

        BigInteger secret = lagrangeInterpolate(shares); // 使用拉格朗日插值恢复多项式在 x=0 处的值（即 a0，也就是密钥）

        return secret.toByteArray();
    }

    /**
     * 计算多项式在 x 处的值
     */
    private static BigInteger evaluatePolynomial(BigInteger[] coefficients, int x) {
        BigInteger result = BigInteger.ZERO;
        BigInteger xPower = BigInteger.ONE;
        BigInteger xBig = BigInteger.valueOf(x);

        for (BigInteger coefficient : coefficients) {
            result = result.add(coefficient.multiply(xPower)).mod(PRIME);
            xPower = xPower.multiply(xBig).mod(PRIME);
        }

        return result;
    }

    /**
     * 拉格朗日插值，计算 f(0) 的值
     */
    private static BigInteger lagrangeInterpolate(List<Share> shares) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < shares.size(); i++) {
            Share share = shares.get(i);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < shares.size(); j++) {
                if (i == j)
                    continue;

                Share otherShare = shares.get(j);
                // 计算拉格朗日基础多项式
                // numerator *= (0 - x_j)
                numerator = numerator.multiply(BigInteger.valueOf(-otherShare.getX())).mod(PRIME);
                // denominator *= (x_i - x_j)
                denominator = denominator.multiply(BigInteger.valueOf(share.getX() - otherShare.getX())).mod(PRIME);
            }

            // 计算 y_i * numerator / denominator
            BigInteger term = share.getY().multiply(numerator).multiply(denominator.modInverse(PRIME)).mod(PRIME);

            result = result.add(term).mod(PRIME);
        }

        return result;
    }
}
