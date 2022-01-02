package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

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
        result.resultFn()
    }
}