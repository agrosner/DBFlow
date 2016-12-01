package com.raizlabs.android.dbflow.processor.utils

/**
 * Description:
 */
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.trim { it <= ' ' }.length == 0 || this == "null"
}

fun String?.capitalizeFirstLetter(): String {
    if (this == null || this.trim { it <= ' ' }.length == 0) {
        return this ?: ""
    }

    return this.substring(0, 1).toUpperCase() + this.substring(1)
}

fun String?.lower(): String {
    if (this == null || this.trim { it <= ' ' }.length == 0) {
        return this ?: ""
    }

    return this.substring(0, 1).toLowerCase() + this.substring(1)
}
