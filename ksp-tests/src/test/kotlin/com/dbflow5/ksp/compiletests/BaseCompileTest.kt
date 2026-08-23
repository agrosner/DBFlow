package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.tschuchort.compiletesting.KotlinCompilation
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
        sources: List<Source>,
        exitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK,
        resultFn: KotlinCompilation.Result.() -> Unit = {}
    ) {
        val result = compilation(temporaryFolder, sources = sources.map {
            it.toKotlinSourceFile(temporaryFolder.root)
        }).compile()
        if (result.exitCode == KotlinCompilation.ExitCode.COMPILATION_ERROR) {
            println(result.messages)
        }
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