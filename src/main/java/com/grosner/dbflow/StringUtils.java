package com.grosner.dbflow;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class StringUtils {

    public static boolean isNotNullOrEmpty(String sourcePath) {
        return sourcePath!=null && !sourcePath.equals("") && sourcePath.length()>0;
    }
}
