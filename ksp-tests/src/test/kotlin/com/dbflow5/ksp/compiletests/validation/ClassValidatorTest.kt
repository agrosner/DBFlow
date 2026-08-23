package com.dbflow5.ksp.compiletests.validation

import com.dbflow5.codegen.shared.validation.ClassCharacteristicsValidator
import com.dbflow5.codegen.shared.validation.PrimaryValidator
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
            assertContains(messages, PrimaryValidator.AT_LEAST_ONE_PRIMARY_MSG)
        }
    }

    @Test
    fun `object class fails`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "badtype",
            """
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.annotation.Table

            @Table
            object SimpleObject {
                @PrimaryKey 
                val name: String = ""
            }
            """.trimIndent()
        )
        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, ClassCharacteristicsValidator.OBJECT_MSG)
        }
    }

    @Test
    fun `abstract class fails`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "badtype",
            """
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.annotation.Table

            @Table
            abstract class SimpleObject {
                @PrimaryKey
                val name: String = ""
            }
            """.trimIndent()
        )
        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, ClassCharacteristicsValidator.ABSTRACT_MSG)
        }
    }


    @Test
    fun `enum class fails`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "badtype",
            """
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.annotation.Table

            @Table
            enum class SimpleObject{
                Name,
                Two,
            }
            """.trimIndent()
        )
        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, ClassCharacteristicsValidator.ENUM_MSG)
        }
    }


}