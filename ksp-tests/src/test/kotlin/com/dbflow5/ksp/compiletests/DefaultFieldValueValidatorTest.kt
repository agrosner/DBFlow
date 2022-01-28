package com.dbflow5.ksp.compiletests

import com.dbflow5.codegen.shared.validation.DefaultFieldValueValidator
import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.dbflow5.ksp.compiletests.sourcefiles.simpleModelFile
import com.tschuchort.compiletesting.KotlinCompilation
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Description:
 */
class DefaultFieldValueValidatorTest : BaseCompileTest() {


    @Test
    fun `throws when specified in reference`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.BadModel",
            """
            package test
            import com.dbflow5.annotation.Column
            import com.dbflow5.annotation.ForeignKey
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.annotation.Table
            
            @Table
            data class BadModel(
                @PrimaryKey val id: String,
                @ForeignKey
                @Column(defaultValue = "55")
                val reference: SimpleModel,
            )
            """.trimIndent()
        )
        assertRun(
            sources = listOf(
                simpleModelFile,
                source,
            ),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR,
        ) {
            assertContains(
                this.messages,
                DefaultFieldValueValidator.ERROR_MSG,
            )
        }
    }
}