package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.Platforms
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

class KSPPlatforms(
    private val processorEnvironment: SymbolProcessorEnvironment,
) : Platforms {
    override val currentPlatform: String
        get() {
            // if all platforms, leave blank
            if (processorEnvironment.platforms.size > 1) {
                return Platforms.All
            }
            return processorEnvironment.platforms[0].platformName
        }
}