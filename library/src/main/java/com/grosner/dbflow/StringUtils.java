package com.grosner.dbflow;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides handy method for strings
 */
public class StringUtils {

    /**
     * Returns true if the string is not null, empty string "", or the length is greater than 0
     *
     * @param inString The string we will check
     * @return
     */
    public static boolean isNotNullOrEmpty(String inString) {
        return inString != null && !inString.equals("") && inString.length() > 0;
    }
}
