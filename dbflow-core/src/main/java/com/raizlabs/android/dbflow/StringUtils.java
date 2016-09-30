package com.raizlabs.android.dbflow;

/**
 * Description: Provides handy method for strings
 */
public class StringUtils {

    /**
     * @return true if the string is not null, empty string "", or the length is greater than 0
     */
    public static boolean isNotNullOrEmpty(String inString) {
        return inString != null && !inString.equals("") && inString.length() > 0;
    }

    /**
     * @return true if the string is null, empty string "", or the length is less than equal to 0
     */
    public static boolean isNullOrEmpty(String inString) {
        return inString == null || inString.equals("") || inString.length() <= 0;
    }
}
