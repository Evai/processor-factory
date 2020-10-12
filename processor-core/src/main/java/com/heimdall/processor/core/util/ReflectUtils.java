package com.heimdall.processor.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author crh
 * @since 2020/10/12
 */
public class ReflectUtils {

    /**
     * 获取类所有字段（包括父类，不包括子类）
     *
     * @param clazz class name
     * @return List
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        if (null == clazz) {
            return Collections.emptyList();
        }
        List<Field> list = new LinkedList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 过滤静态属性
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // 过滤 transient 关键字修饰的属性
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            list.add(field);
        }
        // 获取父类字段
        Class<?> superClass = clazz.getSuperclass();
        if (Object.class.equals(superClass)) {
            return list;
        }
        list.addAll(getAllFields(superClass));
        return list;
    }

}
