package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.dbflow5.ksp.compiletests.sourcefiles.simpleModelFile
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Description:
 */
class ModelViewQueryTests : BaseCompileTest() {
    @Test
    fun `model view query as function`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.ModelView",
            """
            package test
            import com.dbflow5.annotation.ModelView
            import com.dbflow5.annotation.ModelViewQuery
            import com.dbflow5.query.select

            @ModelView(database = TestDatabase::class)
            data class SimpleView(
                val name: String, 
            ) {
                companion object {
                    @JvmStatic
                    @ModelViewQuery
                    fun getQuery() = (select from SimpleModel::class)
                }
            }
            """.trimIndent()
        )
        assertRun(
            sources = listOf(
                dbFile,
                simpleModelFile,
                source
            )
        )
    }
}