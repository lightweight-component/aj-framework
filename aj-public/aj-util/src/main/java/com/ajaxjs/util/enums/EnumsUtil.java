package com.ajaxjs.util.enums;

import com.ajaxjs.util.StrUtil;

import java.util.Optional;

public class EnumsUtil {
    /**
     * 根据枚举代码获取对应的枚举对象
     * 此方法用于处理自定义枚举接口的枚举类型，通过比较枚举代码来找到并返回对应的枚举对象
     * 利用泛型方法，允许传入任何实现了 IEnum 接口的枚举类型，并返回该类型的安全 Optional 对象
     *
     * @param <E>  枚举代码的类型，通常为整型或字符串类型
     * @param <V>  枚举值的类型，可由枚举自行定义
     * @param <T>  枚举类型的泛型参数，表示具体的枚举类型
     * @param code 枚举代码，用于查找对应的枚举对象
     * @param clz  枚举类型的 Class 对象，用于获取枚举常量
     * @return 返回一个 Optional 对象，如果找到对应的枚举对象则返回 Optional.of(value)，否则返回 Optional.empty()
     */
    public static <E, V, T extends IEnum<E, V>> Optional<T> of(E code, Class<T> clz) {
        T[] enums = clz.getEnumConstants(); // 获取枚举类型的所有枚举常量

        for (T value : enums) { // 遍历枚举常量，寻找与给定代码匹配的枚举对象
            if (code == value.getCode()) // 如果代码匹配，则返回对应的枚举对象
                return Optional.of(value);
        }

        return Optional.empty(); // 如果没有找到匹配的枚举对象，则返回空的Optional对象
    }


    /**
     * 根据枚举代码获取对应的枚举消息
     * 此方法利用泛型方法的类型参数，通过给定的枚举代码和枚举类类型，返回枚举代码对应的消息
     * 如果没有找到对应的枚举项，则返回 null
     *
     * @param <E>  枚举代码的类型
     * @param <V>  枚举消息的类型
     * @param <T>  枚举类的类型，该类继承自 IEnum 接口
     * @param code 枚举代码，用于查找对应的枚举项
     * @param cla  枚举类的 Class 对象，用于反射获取枚举项
     * @return 对应枚举代码的消息，如果找不到则返回 null
     */
    public static <E, V, T extends IEnum<E, V>> V ofMsg(E code, Class<T> cla) {
        Optional<T> of = of(code, cla); // 尝试根据枚举代码获取对应的枚举项

        return of.map(IEnum::getMsg).orElse(null); // 如果找到了对应的枚举项，则返回其消息；否则返回null
    }

    /**
     * 根据消息获取枚举代码
     * 该方法用于在枚举类中根据消息找到对应的枚举项，并返回该枚举项的代码
     * 如果消息为空或在枚举类中找不到对应的消息，则返回null
     *
     * @param <E> 枚举代码的类型
     * @param <V> 枚举值的类型
     * @param <T> 枚举类的类型，该类继承了IEnum接口
     * @param msg 消息，用于在枚举类中查找对应的枚举项
     * @param cla 枚举类的Class对象，用于获取枚举项
     * @return 如果找到对应的消息，则返回枚举项的代码；否则返回null
     */
    public static <E, V, T extends IEnum<E, V>> E ofCode(String msg, Class<T> cla) {
        if (StrUtil.isEmptyText(msg))
            return null;

        T[] enums = cla.getEnumConstants();

        // 遍历枚举项，寻找与消息匹配的枚举项
        for (T value : enums) {
            // 如果消息与枚举项的消息匹配，则返回该枚举项的代码
            if (msg.equals(value.getMsg()))
                return value.getCode();
        }

        return null;
    }
}
