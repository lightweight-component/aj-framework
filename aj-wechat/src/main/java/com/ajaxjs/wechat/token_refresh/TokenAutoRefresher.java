package com.ajaxjs.wechat.token_refresh;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Token自动刷新管理器
 * 基于Token实际过期时间，在快要过期时自动刷新Token
 */
public class TokenAutoRefresher {
    /**
     * 默认提前刷新的时间比例（在过期前20%的时间刷新）
     */
    private static final double DEFAULT_REFRESH_RATIO = 0.2;

    /**
     * 最小提前刷新时间（秒），确保不会过于频繁刷新
     */
    private static final int MIN_REFRESH_AHEAD_SECONDS = 10;

    private final ScheduledExecutorService scheduler;
    private final AtomicReference<TokenWithExpire> currentToken;
    private final RefreshToken refreshTokenSupplier;
    private final Consumer<TokenWithExpire> onTokenRefreshed;
    private final double refreshRatio;

    /**
     * 创建Token自动刷新管理器
     *
     * @param refreshTokenSupplier 刷新Token的接口实现
     */
    public TokenAutoRefresher(RefreshToken refreshTokenSupplier) {
        this(refreshTokenSupplier, null);
    }

    /**
     * 创建Token自动刷新管理器
     *
     * @param refreshTokenSupplier 刷新Token的接口实现
     * @param onTokenRefreshed     Token刷新后的回调
     */
    public TokenAutoRefresher(RefreshToken refreshTokenSupplier, Consumer<TokenWithExpire> onTokenRefreshed) {
        this(refreshTokenSupplier, onTokenRefreshed, DEFAULT_REFRESH_RATIO);
    }

    /**
     * 创建Token自动刷新管理器
     *
     * @param refreshTokenSupplier 刷新Token的接口实现
     * @param onTokenRefreshed     Token刷新后的回调
     * @param refreshRatio         提前刷新的时间比例（0-1之间）
     */
    public TokenAutoRefresher(RefreshToken refreshTokenSupplier, Consumer<TokenWithExpire> onTokenRefreshed, double refreshRatio) {
        if (refreshTokenSupplier == null)
            throw new IllegalArgumentException("refreshTokenSupplier cannot be null");

        if (refreshRatio <= 0 || refreshRatio >= 1)
            throw new IllegalArgumentException("refreshRatio must be between 0 and 1");

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "token-auto-refresher");
            thread.setDaemon(true);
            return thread;
        });

        this.currentToken = new AtomicReference<>();
        this.refreshTokenSupplier = refreshTokenSupplier;
        this.onTokenRefreshed = onTokenRefreshed;
        this.refreshRatio = refreshRatio;
    }

    /**
     * 初始化并启动自动刷新
     * 首次会立即获取Token，然后根据过期时间设置定时刷新
     *
     * @return 当前实例，支持链式调用
     */
    public TokenAutoRefresher start() {
        refreshToken();  // 首次获取Token

        return this;
    }

    /**
     * 获取当前有效的Token
     *
     * @return 当前Token，如果未初始化则可能返回null
     */
    public String getToken() {
        TokenWithExpire token = currentToken.get();

        return token != null ? token.getToken() : null;
    }

    /**
     * 获取当前Token信息（包含过期时间）
     *
     * @return 当前Token信息
     */
    public TokenWithExpire getTokenWithExpire() {
        return currentToken.get();
    }

    /**
     * 手动触发刷新Token
     *
     * @return 新的Token
     */
    public TokenWithExpire refreshToken() {
        TokenWithExpire newToken = refreshTokenSupplier.get();

        if (newToken == null)
            throw new IllegalStateException("Refresh token returned null");

        currentToken.set(newToken);

        if (onTokenRefreshed != null)  // 回调通知
            onTokenRefreshed.accept(newToken);

        scheduleNextRefresh(newToken); // 安排下一次刷新

        return newToken;
    }

    /**
     * 计算并安排下一次刷新
     *
     * @param token 当前Token
     */
    private void scheduleNextRefresh(TokenWithExpire token) {
        int expireSeconds = token.getExpire();

        // 计算提前刷新的时间
        long refreshAheadSeconds = Math.max((long) (expireSeconds * refreshRatio), MIN_REFRESH_AHEAD_SECONDS);

        // 确保不会设置负数或零延迟
        long delaySeconds = Math.max(expireSeconds - refreshAheadSeconds, 1);

        scheduler.schedule(this::refreshToken, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * 停止自动刷新
     */
    public void stop() {
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                scheduler.shutdownNow();
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查当前Token是否即将过期
     *
     * @return 如果Token即将过期返回true
     */
    public boolean isTokenExpiringSoon() {
        TokenWithExpire token = currentToken.get();

        if (token == null)
            return true;

        // 如果剩余时间小于最小提前刷新时间，则认为即将过期
        return token.getExpire() <= MIN_REFRESH_AHEAD_SECONDS;
    }
}
