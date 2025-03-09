package com.ajaxjs.rag.utils;

import java.util.stream.IntStream;

public class DistanceUtils {

    /**
     * 计算两个向量之间的平方误差距离
     *
     * @param v1 第一个向量
     * @param v2 第二个向量
     * @return 平方误差距离
     */
    public static double squaredErrorDistance(double[] v1, double[] v2) {
        return IntStream.range(0, v1.length)
                .mapToDouble(i -> Math.pow(v1[i] - v2[i], 2))
                .sum();
    }

    /**
     * 计算两个向量之间的绝对距离误差
     *
     * @param v1 第一个向量
     * @param v2 第二个向量
     * @return 绝对距离误差
     */
    public static double absoluteErrorDistance(double[] v1, double[] v2) {
        return IntStream.range(0, v1.length)
                .mapToDouble(i -> Math.abs(v1[i] - v2[i]))
                .sum();
    }

    /**
     * 计算两个向量之间的余弦相似度，然后取 1 - 余弦相似度得到余弦误差
     *
     * @param v1 第一个向量
     * @param v2 第二个向量
     * @return 余弦误差
     */
    public static double cosineError(double[] v1, double[] v2) {
        double dotProduct = IntStream.range(0, v1.length)
                .mapToDouble(i -> v1[i] * v2[i])
                .sum();
        double magnitudeV1 = Math.sqrt(IntStream.range(0, v1.length)
                .mapToDouble(i -> v1[i] * v1[i])
                .sum());
        double magnitudeV2 = Math.sqrt(IntStream.range(0, v2.length)
                .mapToDouble(i -> v2[i] * v2[i])
                .sum());
        double cosineSimilarity = dotProduct / (magnitudeV1 * magnitudeV2);

        return 1 - cosineSimilarity;
    }
}