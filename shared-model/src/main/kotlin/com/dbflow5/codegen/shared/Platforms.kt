package com.dbflow5.codegen.shared

/**
 * Provided by processors by which platforms are supported.
 */
interface Platforms {

    val currentPlatform: String

    companion object {
        const val All = "ALL"
        const val JVM = "JVM"
    }
}
