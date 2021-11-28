package com.dbflow5.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.rules.TemporaryFolder

/**
 * Description: Creates compilation for testing.
 */
fun compilation(
    temporaryFolder: TemporaryFolder,
    sources: List<SourceFile>
): KotlinCompilation {
    return KotlinCompilation().apply {
        workingDir = temporaryFolder.root
        inheritClassPath = true
        this.sources = sources
        symbolProcessorProviders = listOf(DBFlowSymbolProcessorProvider())
    }
}