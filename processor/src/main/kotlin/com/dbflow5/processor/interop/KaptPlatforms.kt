package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.Platforms

class KaptPlatforms : Platforms {

    /**
     * Kapt only runs on JVM sources.
     */
    override val currentPlatform = Platforms.JVM
}
