package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

/**
 * Description:
 */
class TypeConverterTests {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `standard type converter`() {
        val source = SourceFile.kotlin(
            "TypeConverterExample.kt",
            """
            package test
            import com.dbflow5.annotation.TypeConverter

            data class SimpleType(val name: String)
            
            @TypeConverter
            class SampleConverter: com.dbflow5.converter.TypeConverter<String, SimpleType>() {
    
                override fun getDBValue(model: SimpleType): String {
                    return model.name
                }
                override fun getModelValue(data: String): SimpleType{
                    return SimpleType(data)
                }
            }
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }
}