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

    @Test
    fun `type converter field`() {
        val source = SourceFile.kotlin(
            "TypeConverterExample.kt",
            """
            package test
            import com.dbflow5.annotation.*
            import com.dbflow5.config.DBFlowDatabase

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
            
            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @Table(database = TestDatabase::class)
            class SimpleModel(
                @PrimaryKey @Column val name: String,
                @Column(typeConverter = SampleConverter::class) val sample: SimpleType,
            )  
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `nested converters`() {
        @Language("kotlin") val source = SourceFile.kotlin("Converters.kt",
        """
         import com.dbflow5.annotation.Database
         import com.dbflow5.config.DBFlowDatabase
         import com.dbflow5.converter.TypeConverter
         import com.dbflow5.data.Blob

         data class MyBlob(val blob: Blob)

         @com.dbflow5.annotation.TypeConverter
         class CustomBlobConverter: TypeConverter<Blob, MyBlob>() {

            override fun getDBValue(model: MyBlob) = Blob(model.blob)
    
            override fun getModelValue(data: Blob) = MyBlob(data.blob)
        }

        @Database(version = 1)
        abstract class TestDatabase : DBFlowDatabase()
        """.trimIndent())

        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}