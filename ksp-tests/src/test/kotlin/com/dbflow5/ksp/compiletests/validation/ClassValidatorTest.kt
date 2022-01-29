package com.dbflow5.ksp.compiletests.validation

import com.dbflow5.codegen.shared.validation.ClassToFieldValidator
import com.dbflow5.ksp.compiletests.BaseCompileTest
import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.tschuchort.compiletesting.KotlinCompilation
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Description:
 */
class ClassValidatorTest : BaseCompileTest() {

    @Test
    fun `empty fields check`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "empty",
            """
            import com.dbflow5.annotation.Table
            @Table    
            class Empty()
            """.trimIndent()
        )

        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, ClassToFieldValidator.EMPTY_FIELDS_MSG)
        }
    }
}