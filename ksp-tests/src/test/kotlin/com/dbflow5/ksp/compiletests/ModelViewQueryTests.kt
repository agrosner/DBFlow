package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.assertEquals

/**
 * Description:
 */
class ModelViewQueryTests {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `model view query as function`() {
        @Language("kotlin") val source = SourceFile.kotlin(
            "ModelView.kt",
            """
            import com.dbflow5.annotation.Column
            import com.dbflow5.annotation.ModelView
            import com.dbflow5.annotation.ModelViewQuery
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.Database
            import com.dbflow5.query.select

            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()

            @Table(database = TestDatabase::class)
            data class SimpleModel(@Column val name: String)

            @ModelView(database = TestDatabase::class)
            data class SimpleView(
                @Column val name: String, 
            ) {
                companion object {
                    @JvmStatic
                    @ModelViewQuery
                    fun getQuery() = (select from SimpleModel::class)
                }
            }
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}