package com.dbflow5.ksp.compiletests.validation

import com.dbflow5.codegen.shared.validation.TypeConverterValidator
import com.dbflow5.ksp.compiletests.BaseCompileTest
import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.tschuchort.compiletesting.KotlinCompilation
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Description:
 */
class TypeConverterValidatorTest : BaseCompileTest() {

    @Test
    fun `checks bounds on type converter`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "type",
            """
            import com.dbflow5.converter.TypeConverter

            @com.dbflow5.annotation.TypeConverter
            class SameTypeConverter: TypeConverter<String, String>() {
                override fun getDBValue(model: String): String { TODO("Not yet implemented") }
                override fun getModelValue(data: String): String { TODO("Not yet implemented") }
            }
            """.trimIndent()
        )

        assertRun(
            sources = listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, TypeConverterValidator.SAME_TYPE_MSG)
        }
    }

    @Test
    fun `disallow extends typeconverter`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "Converters",
            """
            import com.dbflow5.converter.TypeConverter
            import com.dbflow5.data.Blob

            abstract class SpecialConverter<T>: TypeConverter<String, T>() 

            @com.dbflow5.annotation.TypeConverter
            class ActualConverter: SpecialConverter<Blob> {
              override fun getDBValue(model: Blob): String{TODO("Not yet implemented") }
              override fun getModelValue(data: String): Blob{TODO("Not yet implemented") }    
            }
            """
        )

        assertRun(
            sources = listOf(source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        ) {
            assertContains(messages, "Error typeConverter super for")
        }
    }
}