package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest

/**
 * Description:
 */
class ModelViewQueryTests : BaseCompileTest() {
    @Test
    fun `model view query as function`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "ModelView.kt",
            """
            package test
            import com.dbflow5.annotation.Column
            import com.dbflow5.annotation.ModelView
            import com.dbflow5.annotation.ModelViewQuery
            import com.dbflow5.annotation.Table
            import com.dbflow5.query.select

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
        assertRun(sources = listOf(dbFile, source))
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}