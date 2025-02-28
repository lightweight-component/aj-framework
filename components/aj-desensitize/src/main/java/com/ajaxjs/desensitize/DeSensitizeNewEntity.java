package com.ajaxjs.desensitize;

import com.ajaxjs.desensitize.annotation.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对实体类进行脱敏，返回原来的实体类对象
 */
public class DeSensitizeNewEntity {
    /**
     * 对指定实体类中标记类脱敏注解的字段进行脱敏；支持指定外层包装类未标记类脱敏注解的字段，但对内层进行脱敏
     *
     * @param entity    实体类|普通对象
     * @param packClass 需脱敏的实体类对象外层包装类
     * @param <T>       实体类类型
     * @return 对实体类进行脱敏，返回原来的实体类对象
     */
    protected static <T> T acquire(T entity, Class<?>... packClass) {
        if (Utils.isFinal(entity))
            return entity;

        if (entity instanceof Collection) {
            for (Object o : (Collection<?>) entity)
                acquire(o, packClass);
        } else if (entity instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) entity).entrySet())
                acquire(entry.getValue(), packClass);
        } else if (entity.getClass().isArray()) {
            if (!entity.getClass().getComponentType().isPrimitive()) {
                for (Object v : (Object[]) entity)
                    acquire(v, packClass);
            }
        } else if (entity.getClass().isAnnotationPresent(DesensitizeModel.class))
            doSetField(entity);
        else if (packClass.length > 0 && entity.getClass().isAssignableFrom(packClass[0]))
            doSetField(entity, Utils.remove(packClass, 0));

        return entity;
    }

    /**
     * 对实体类entity的属性及父类的属性遍历并对符合条件的属性进行多语言翻译
     *
     * @param entity 实体类对象
     * @param <T>    实体类类型
     */
    protected static <T> void doSetField(T entity, Class<?>... packClass) {
        List<Field> fields = Utils.getAllFields(entity.getClass());

        for (Field field : fields) {
            if (Utils.isModifierFinal(field))
                continue;

            field.setAccessible(true);
            Object value = Utils.getValue(field, entity);

            if (checkNullValue(field, value)) {
                Utils.setField(field, entity, null);
                continue;
            }

            if (value instanceof String) {
                doGetEntityStr(field, entity, value);
            } else if (value instanceof Collection) {
                doGetEntityColl(field, entity, value);
            } else if (value instanceof Map) {
                doGetEntityMap(field, entity, value);
            } else if (value.getClass().isArray()) {
                doGetEntityArray(field, entity, value);
            } else
                acquire(value, packClass);
        }

        doGetEntityComplex(entity);
    }

    /**
     * 判定Field字段值是否置为null
     * -------------------------------------------
     * 1.value为null,则返回true
     * 2.field字段类型为原始数据类型，如int、boolean、double等，则返回false
     * 3.field被JsonNullField注解标注，则返回true
     * 4.其它场景都返回false
     * -------------------------------------------
     *
     * @param field 字段对象
     * @param value 字段值
     * @return true-置为null, false-按原值展示
     */
    protected static boolean checkNullValue(Field field, Object value) {
        if (value == null)
            return true;
        else if (field.getType().isPrimitive())
            return false;
        else return field.isAnnotationPresent(DesensitizeNullProperty.class);
    }

    /**
     * 对字符串进行多语言支持
     *
     * @param field  实体类属性对象
     * @param entity 实体类对象
     * @param value  属性值对象
     * @param <T>    实体类类型
     */
    protected static <T> void doGetEntityStr(Field field, T entity, Object value) {
        if (field.isAnnotationPresent(DesensitizeProperty.class)) {
            String _value = DataMask.doGetProperty((String) value, field.getAnnotation(DesensitizeProperty.class).value());
            Utils.setField(field, entity, _value);
        } else
            acquire(value);
    }


    /**
     * 对Collection集合中存储是字符串、实体对象进行多语言支持
     *
     * @param field  实体类属性对象
     * @param entity 实体类对象
     * @param value  属性值对象
     * @param <T>    实体类类型
     */
    protected static <T> void doGetEntityColl(Field field, T entity, Object value) {
        Collection<Object> list = null;
        Collection<?> collection = ((Collection<?>) value);

        for (Object v : collection) {
            if (Objects.isNull(v))
                continue;

            if ((v instanceof String) && field.isAnnotationPresent(DesensitizeProperty.class)) {
                list = (list == null) ? new ArrayList<>() : list;
                list.add(DataMask.doGetProperty((String) v, field.getAnnotation(DesensitizeProperty.class).value()));
            } else
                acquire(v);
        }

        if (Objects.nonNull(list))
            Utils.setField(field, entity, list);
    }

    /**
     * 对Map集合中存储是字符串、实体对象进行脱敏支持
     *
     * @param field  实体类属性对象
     * @param entity 实体类对象
     * @param value  属性值对象
     * @param <T>    实体类类型
     */
    @SuppressWarnings("unused")
    protected static <T> void doGetEntityMap(Field field, T entity, Object value) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> dMap = (Map<Object, Object>) value;

        for (Map.Entry<Object, Object> entry : dMap.entrySet()) {
            Object key = entry.getKey();
            Object v = entry.getValue();

            if (Objects.isNull(v))
                continue;

            if (v instanceof String) {
                if (field.isAnnotationPresent(DesensitizeMapProperty.class)) {
                    DesensitizeMapProperty desensitizeMapProperty = field.getAnnotation(DesensitizeMapProperty.class);
                    int index = (key instanceof String) ? Arrays.asList(desensitizeMapProperty.keys()).indexOf(key) : -1;

                    if (index < 0)
                        continue;

                    DesensitizeType type = DesensitizeType.DEFAULT;
                    if (index <= desensitizeMapProperty.types().length - 1)
                        type = desensitizeMapProperty.types()[index];

                    dMap.put(key, DataMask.doGetProperty((String) v, type));
                    continue;
                } else if (field.isAnnotationPresent(DesensitizeProperty.class)) {
                    dMap.put(key, DataMask.doGetProperty((String) v, field.getAnnotation(DesensitizeProperty.class).value()));
                    continue;
                }
            }

            acquire(value);
        }
    }

    /**
     * 对数组中存储是字符串、实体对象进行多语言支持
     *
     * @param field  实体类属性对象
     * @param entity 实体类对象
     * @param value  属性值对象
     * @param <T>    实体类类型
     */
    @SuppressWarnings("unused")
    protected static <T> void doGetEntityArray(Field field, T entity, Object value) {
        if (value.getClass().getComponentType().isPrimitive())
            return;

        Object[] arrays = ((Object[]) value);

        for (int i = 0; i < arrays.length; i++) {
            Object v = arrays[i];

            if (Objects.isNull(v))
                continue;

            if ((v instanceof String) && field.isAnnotationPresent(DesensitizeProperty.class))
                arrays[i] = DataMask.doGetProperty((String) v, field.getAnnotation(DesensitizeProperty.class).value());
            else
                acquire(value);
        }
    }

    /**
     * @param entity 实体类对象
     * @param <T>    实体类类型
     */
    protected static <T> void doGetEntityComplex(T entity) {
        List<Field> fields = Utils.getFieldsWithAnnotation(entity.getClass(), DesensitizeComplexProperty.class);


        for (Field field : fields) {
            field.setAccessible(true);
            Object value = Utils.getValue(field, entity);

            if (Objects.isNull(value))
                continue;

            DesensitizeComplexProperty desensitizeComplexProperty = field.getAnnotation(DesensitizeComplexProperty.class);
            if (Objects.isNull(desensitizeComplexProperty.value()))
                return;

            Field flexField = Utils.getField(entity.getClass(), desensitizeComplexProperty.value(), true);
            if (Objects.isNull(flexField))
                return;

            Object flexValue = Utils.getValue(flexField, entity);
            if (Objects.isNull(flexValue) || !(flexValue instanceof String))
                return;

            int index = Arrays.asList(desensitizeComplexProperty.keys()).indexOf((String) value);
            if (index < 0)
                return;

            DesensitizeType type = DesensitizeType.DEFAULT;
            if (index <= desensitizeComplexProperty.types().length - 1)
                type = desensitizeComplexProperty.types()[index];

            Utils.setField(flexField, entity, DataMask.doGetProperty((String) flexValue, type));
        }

    }
}
