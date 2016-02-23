package com.raizlabs.android.dbflow.processor.utils;

/**
 * Description:
 */
public class StringUtils {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0 || str.equals("null");
    }

    public static String capitalize(String str) {
        if (str == null || str.trim().length() == 0) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String lower(String str) {
        if (str == null || str.trim().length() == 0) {
            return str;
        }

        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
