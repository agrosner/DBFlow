package com.raizlabs.android.dbflow.processor.utils;

/**
 * Description:
 */
public class StringUtils {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0 || str.equals("null");
    }
}
