package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Description:
 */
class TypeConverterTests : BaseCompileTest() {
    @Test
    fun `standard type converter`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.TypeConverterExample",
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
        assertRun(sources = listOf(source))
    }

    @Test
    fun `type converter field`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.TypeConverterExample",
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
            
            @Table(database = TestDatabase::class)
            class SimpleModel(
                @PrimaryKey @Column val name: String,
                @Column(typeConverter = SampleConverter::class) val sample: SimpleType,
            )  
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }

    @Test
    fun `nested converters`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "Converters",
            """
         import com.dbflow5.converter.TypeConverter
         import com.dbflow5.data.Blob

         data class MyBlob(val blob: Blob)

         @com.dbflow5.annotation.TypeConverter
         class CustomBlobConverter: TypeConverter<Blob, MyBlob>() {

            override fun getDBValue(model: MyBlob) = Blob(model.blob)
    
            override fun getModelValue(data: Blob) = MyBlob(data.blob)
        }

        """.trimIndent()
        )

        assertRun(sources = listOf(source))
    }
}
