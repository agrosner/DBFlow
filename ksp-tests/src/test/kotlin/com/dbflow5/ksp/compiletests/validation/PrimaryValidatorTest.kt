package com.dbflow5.ksp.compiletests.validation

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
class PrimaryValidatorTest : BaseCompileTest() {

    @Test
    fun `multiple autoincrement fails`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "BadSource",
            """
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table

                @Table
                data class PrimaryModel(
                    @PrimaryKey(autoincrement = true)
                    val id: Int,
                    @PrimaryKey(autoincrement = true)
                    val id2: Int,
                )
            """.trimIndent()
        )

        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, PrimaryValidator.MORE_THAN_ONE_PRIMARY_AUTO_MSG)
        }
    }

    @Test
    fun `mix and match primary keys`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "mixy",
            """
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table
                
                @Table
                data class Mixed(
                    @PrimaryKey
                    val id: String,
                    @PrimaryKey(autoincrement = true)
                    val name: String,
                )
            """.trimIndent()
        )

        assertRun(
            listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, PrimaryValidator.MIX_AND_MATCH_PRIMARY_MSG)
        }
    }
}