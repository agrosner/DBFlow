package com.raizlabs.android.dbflow.processor.model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ReflectionUtils {


    public static boolean isSubclassOf(String columnFieldType, Class<?> enumClass) {
        boolean isSubClass = false;
        try {
            Class type = Class.forName(columnFieldType);
            isSubClass = type.getSuperclass() != null && (type.getSuperclass().equals(enumClass) ||
                    isSubclassOf(type.getSuperclass().getName(), enumClass));
        } catch (ClassNotFoundException e) {
        }
        return isSubClass;
    }
}
