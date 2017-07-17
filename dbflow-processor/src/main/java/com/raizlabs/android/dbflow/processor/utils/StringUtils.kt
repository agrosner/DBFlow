package com.raizlabs.android.dbflow.processor.utils

/**
 * Description:
 */
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.trim { it <= ' ' }.isEmpty() || this == "null"
}

fun String?.capitalizeFirstLetter(): String {
    if (this == null || this.trim { it <= ' ' }.isEmpty()) {
        return this ?: ""
    }

    return this.capitalize()
}

fun String?.lower(): String {
    if (this == null || this.trim { it <= ' ' }.isEmpty()) {
        return this ?: ""
    }

    return this.substring(0, 1).toLowerCase() + this.substring(1)
}
