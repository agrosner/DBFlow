package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.dbflow5.ksp.compiletests.sourcefiles.simpleModelFile
import org.intellij.lang.annotations.Language
import kotlin.test.Test

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

            @ModelView(database = TestDatabase::class, query = "SELECT * FROM `SimpleModel`")
            data class SimpleView(
                val name: String, 
            )
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