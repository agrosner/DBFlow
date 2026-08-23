package com.dbflow5.compiler

import com.dbflow5.compiler.fir.DBFlowFirExtensionRegistrar
import com.dbflow5.compiler.ir.DBFlowIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Metro-style K2 plugin entry: FIR for analysis, IR for code generation.
 */
@OptIn(ExperimentalCompilerApi::class)
class DBFlowCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override val pluginId: String
        get() = DBFlowCommandLineProcessor.PLUGIN_ID

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val generatedDir = configuration.get(GENERATED_DIR_KEY)
        val generateOnly =
            configuration.get(MODE_KEY) == DBFlowCommandLineProcessor.MODE_GENERATE
        FirExtensionRegistrarAdapter.registerExtension(DBFlowFirExtensionRegistrar())
        IrGenerationExtension.registerExtension(
            DBFlowIrGenerationExtension(
                generatedDir = generatedDir,
                generateOnly = generateOnly,
            )
        )
    }
}
