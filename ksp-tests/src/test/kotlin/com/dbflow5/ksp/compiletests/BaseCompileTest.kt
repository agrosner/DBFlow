package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Description:
 */
open class BaseCompileTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    fun assertRun(
        sources: List<SourceFile>,
        exitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK,
        resultFn: KotlinCompilation.Result.() -> Unit = {}
    ) {
        val result = compilation(temporaryFolder, sources = sources).compile()
        assertEquals(exitCode, result.exitCode)
        if (exitCode == KotlinCompilation.ExitCode.OK) {
            val generatedSources = result.kspGeneratedSources
            assertTrue(generatedSources.isNotEmpty())
        }
        result.resultFn()
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}