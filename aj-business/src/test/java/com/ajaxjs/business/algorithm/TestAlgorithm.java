package com.ajaxjs.business.algorithm;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class TestAlgorithm {
    @Test
    public void testSimpleCalc() {
        List<String> array = new ArrayList<>();
        try (Scanner scanner = new Scanner(System.in)) {
            // 捕获输入
            while (true) {
                String string = scanner.nextLine();
                // 输入'END'时，结束输入
                if (!"END".equals(string)) array.add(string);
                else break;
            }

            // 计算并打印
            SimpleCalc.print(array);
        }
    }

    @Test
    public void testWeightRandom() {
        WeightRandom.Item[] items = new WeightRandom.Item[]{new WeightRandom.Item("A", 0.1), new WeightRandom.Item("B", 0.2), new WeightRandom.Item("C", 0.65), new WeightRandom.Item("D", 0.05),};
        WeightRandom weightRandom = new WeightRandom(items);

        for (int i = 0; i < 10; i++)
            System.out.println(MessageFormat.format("员工{0}的绩效得分：{1}", (i + 1), weightRandom.nextItem()));
    }
}
