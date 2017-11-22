package com.raizlabs.android.dbflow

/**
 * Description: Provides handy method for strings
 */
object StringUtils {

    /**
     * @return true if the string is not null, empty string "", or the length is greater than 0
     */
    @JvmStatic
    fun isNotNullOrEmpty(inString: String?): Boolean =
            inString != null && inString != "" && inString.isNotEmpty()

    /**
     * @return true if the string is null, empty string "", or the length is less than equal to 0
     */
    @JvmStatic
    fun isNullOrEmpty(inString: String?): Boolean =
            inString == null || inString == "" || inString.isEmpty()
}
