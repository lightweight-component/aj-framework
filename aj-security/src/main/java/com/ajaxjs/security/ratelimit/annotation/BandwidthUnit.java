package com.ajaxjs.security.ratelimit.annotation;


/**
 * 带宽单位枚举
 */
public enum BandwidthUnit {
    /**
     * Bytes.
     */
    B(1),
    /**
     * Kibibytes.
     */
    KB(1024),
    /**
     * Mebibytes.
     */
    MB(1024 * 1024),
    /**
     * Gibibytes.
     */
    GB(1024 * 1024 * 1024);

    /**
     * Stores the bytes per second value.
     */
    private final long bytesPerSecond;

    BandwidthUnit(long bytesPerSecond) {
        this.bytesPerSecond = bytesPerSecond;
    }

    /**
     * Executes the to bytes per second operation.
     *
     * @param value the value parameter.
     * @return the operation result.
     */
    public long toBytesPerSecond(long value) {
        return value * bytesPerSecond;
    }

    /**
     * Executes the get bytes per unit operation.
     *
     * @return the operation result.
     */
    public long getBytesPerUnit() {
        return bytesPerSecond;
    }

    /**
     * Executes the format bytes operation.
     *
     * @param bytes the bytes parameter.
     * @return the operation result.
     */
    public static String formatBytes(long bytes) {
        if (bytes < KB.getBytesPerUnit())
            return bytes + " B";
        else if (bytes < MB.getBytesPerUnit())
            return String.format("%.2f KB", bytes / (double) KB.getBytesPerUnit());
        else if (bytes < GB.getBytesPerUnit())
            return String.format("%.2f MB", bytes / (double) MB.getBytesPerUnit());
        else
            return String.format("%.2f GB", bytes / (double) GB.getBytesPerUnit());
    }
}
