package com.ajaxjs.desensitize;

import com.ajaxjs.desensitize.annotation.DesensitizeComplexProperty;
import com.ajaxjs.desensitize.annotation.DesensitizeMapProperty;
import com.ajaxjs.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.desensitize.annotation.DesensitizeProperty;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对实体类镜像脱敏，返回结构相同的非同一个对象
 */
public class DeSensitize {
    /**
     * 对实体类镜像脱敏，返回结构相同的非同一个对象
     *
     * @param entity    需要脱敏的实体类对象，如果是数据值类型则直接返回
     * @param packClass 需脱敏的实体类对象外层包装类
     * @return 脱敏后的实体类对象
     */
    public static Object acquire(Object entity, Class<?>... packClass) {
        if (Utils.isFinal(entity))
            return entity;

        if (entity instanceof Collection) {
            Collection<Object> coll = new ArrayList<>();
            for (Object o : (Collection<?>) entity)
                coll.add(acquire(o, packClass));

            return coll;
        } else if (entity instanceof Map) {
            Map<Object, Object> dMap = new HashMap<>();

            for (Map.Entry<?, ?> entry : ((Map<?, ?>) entity).entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                dMap.put(key, acquire(value, packClass));
            }

            return dMap;
        } else if (entity.getClass().isArray()) {
            if (entity.getClass().getComponentType().isPrimitive())
                return entity;
            else {
                Object[] v = (Object[]) entity;
                Object[] t = new Object[v.length];

                for (int i = 0; i < v.length; i++)
                    t[i] = acquire(v[i], packClass);

                return t;
            }
        } else if (entity.getClass().isAnnotationPresent(DesensitizeModel.class))
            return doSetField(entity);
        else if (packClass.length > 0 && entity.getClass().isAssignableFrom(packClass[0]))
            return doSetField(entity, Utils.remove(packClass, 0));

        return entity;
    }

    /**
     * 获取实体类对象脱敏后的对象
     *
     * @param entity 需要脱敏的实体类对象
     * @return 实体类属性脱敏后的集合对象
     */
    protected static Map<String, Object> doSetField(Object entity, Class<?>... packClass) {
        Map<String, Object> fieldMap = new HashMap<>();
        List<Field> fields = Utils.getAllFields(entity.getClass());


        for (Field field : fields) {
            if (Utils.isModifierFinal(field))
                continue;

            field.setAccessible(true);
            String name = field.getName();
            Object value = Utils.getValue(field, entity);

            if (DeSensitizeNewEntity.checkNullValue(field, value)) {
                fieldMap.put(name, null);
                continue;
            }

            if (value instanceof String)
                fieldMap.put(name, doGetEntityStr(field, value));
            else if (value instanceof Collection)
                fieldMap.put(name, doGetEntityColl(field, value));
            else if (value instanceof Map)
                fieldMap.put(name, doGetEntityMap(field, value));
            else if (value.getClass().isArray())
                fieldMap.put(name, doGetEntityArray(field, value));
            else
                fieldMap.put(name, acquire(value, packClass));
        }

        fieldMap.putAll(doGetEntityComplex(entity));
        return fieldMap;
    }


    /**
     * @param field 实体类属性对象
     * @param value 属性值
     * @return 脱敏后的数据对象
     */
    protected static Object doGetEntityStr(Field field, Object value) {
        if (field.isAnnotationPresent(DesensitizeProperty.class))
            return DataMask.doGetProperty((String) value, field.getAnnotation(DesensitizeProperty.class).value());
        else
            return acquire(value);
    }

    /**
     * @param field 实体类属性对象
     * @param value 属性值
     * @return 脱敏后的数据对象
     */
    protected static Object doGetEntityColl(Field field, Object value) {
        Collection<Object> list = new ArrayList<>();
        Collection<?> collection = (Collection<?>) value;

        for (Object v : collection) {
            if (Objects.isNull(v))
                list.add(null);
            else if ((v instanceof String) && field.isAnnotationPresent(DesensitizeProperty.class))
                list.add(DataMask.doGetProperty((String) v, field.getAnnotation(DesensitizeProperty.class).value()));
            else
                list.add(acquire(v));
        }

        return list;
    }

    /**
     * @param field 实体类属性对象
     * @param value 属性值
     * @return 脱敏后的数据对象
     */
    protected static Object doGetEntityMap(Field field, Object value) {
        Map<Object, Object> dMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<Object, Object> entryMap = ((Map<Object, Object>) value);

        for (Map.Entry<Object, Object> entry : entryMap.entrySet()) {
            Object key = entry.getKey();
            Object v = entry.getValue();

            if (Objects.isNull(v)) {
                dMap.put(key, null);
                continue;
            }

            if (v instanceof String) {
                if (field.isAnnotationPresent(DesensitizeMapProperty.class)) {
                    DesensitizeMapProperty desensitizeMapProperty = field.getAnnotation(DesensitizeMapProperty.class);
                    int index = (key instanceof String) ? Arrays.asList(desensitizeMapProperty.keys()).indexOf(key) : -1;

                    if (index < 0) {
                        dMap.put(key, acquire(v));
                        continue;
                    }

                    DesensitizeType type = DesensitizeType.DEFAULT;
                    if (index <= desensitizeMapProperty.types().length - 1)
                        type = desensitizeMapProperty.types()[index];

                    dMap.put(key, DataMask.doGetProperty((String) v, type));
                    continue;
                } else if (field.isAnnotationPresent(DesensitizeProperty.class))
                    dMap.put(key, DataMask.doGetProperty((String) v, field.getAnnotation(DesensitizeProperty.class).value()));
            }

            dMap.put(key, acquire(v));
        }

        return dMap;
    }

    /**
     * @param field 实体类属性对象
     * @param value 属性值
     * @return 脱敏后的数据对象
     */
    protected static Object doGetEntityArray(Field field, Object value) {
        if (value.getClass().getComponentType().isPrimitive())
            return value;
        else {
            Object[] v = (Object[]) value;
            Object[] t = new Object[v.length];

            for (int i = 0; i < v.length; i++) {
                if (Objects.isNull(v[i]))
                    t[i] = null;
                else if ((v[i] instanceof String) && field.isAnnotationPresent(DesensitizeProperty.class))
                    t[i] = DataMask.doGetProperty((String) v[i], field.getAnnotation(DesensitizeProperty.class).value());
                else
                    t[i] = acquire(v[i]);
            }

            return t;
        }
    }

    /**
     * 灵活复杂类型字段脱敏
     *
     * @param entity 实体类
     * @return 复杂类型字段脱敏后的数据集合
     */
    protected static Map<String, Object> doGetEntityComplex(Object entity) {
        Map<String, Object> flexFieldMap = null;
        List<Field> fields = Utils.getFieldsWithAnnotation(entity.getClass(), DesensitizeComplexProperty.class);

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = Utils.getValue(field, entity);

            if (Objects.isNull(value))
                continue;

            DesensitizeComplexProperty desensitizeComplexProperty = field.getAnnotation(DesensitizeComplexProperty.class);
            if (Objects.isNull(desensitizeComplexProperty.value()))
                continue;

            Field flexField = Utils.getField(entity.getClass(), desensitizeComplexProperty.value(), true);
            if (Objects.isNull(flexField))
                continue;

            Object flexValue = Utils.getValue(flexField, entity);
            if (Objects.isNull(flexValue) || !(flexValue instanceof String))
                continue;

            int index = Arrays.asList(desensitizeComplexProperty.keys()).indexOf((String) value);
            if (index < 0)
                continue;

            DesensitizeType type = DesensitizeType.DEFAULT;
            if (index <= desensitizeComplexProperty.types().length - 1)
                type = desensitizeComplexProperty.types()[index];

            flexFieldMap = Objects.isNull(flexFieldMap) ? new HashMap<>() : flexFieldMap;
            flexFieldMap.put(desensitizeComplexProperty.value(), DataMask.doGetProperty((String) flexValue, type));
        }

        return Objects.isNull(flexFieldMap) ? Collections.emptyMap() : flexFieldMap;
    }
}
