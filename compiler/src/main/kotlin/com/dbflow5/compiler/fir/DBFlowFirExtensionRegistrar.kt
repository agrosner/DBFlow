package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * FIR registration point (Metro: analysis and validation).
 * Generation currently happens in IR so we can reuse the existing KotlinPoet writers.
 */
class DBFlowFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::DBFlowFirCheckersExtension
        +::FirTableCompanionGenerationExtension
        +::FirTableCompanionSupertypeExtension
    }
}
