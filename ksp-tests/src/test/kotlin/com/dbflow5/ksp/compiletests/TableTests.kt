package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import kotlin.test.Test

/**
 * Description:
 */
class TableTests : BaseCompileTest() {

    @Test
    fun `basic table`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey

            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: String)  
            """.trimIndent()
        )
        assertRun(
            sources = listOf(dbFile, source)
        )
    }

    @Test
    fun `foreign key table`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "ForeignKeyTable.kt",
            """
            package test
            import com.dbflow5.annotation.ForeignKey
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            
            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: String)
              
            @Table(database = TestDatabase::class)
            class ForeignKeyModel(
                @PrimaryKey val name: String,
                @ForeignKey val model: SimpleModel,
            )

            """.trimIndent()
        )
        assertRun(
            sources = listOf(dbFile, source)
        )
    }

    @Test
    fun `inline class table`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            
            @JvmInline
            value class Name(val value: String)
            
            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: Name)  
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }

    @Test
    fun `property has global type converter`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.data.Blob

            @Table(database = TestDatabase::class)
            class SimpleModel(
                @PrimaryKey val name: String,
                val blob: Blob,
            )  
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }

    @Test
    fun `java class with constructor should apply constructor`() {
        @Language("java")
        val source = SourceFile.java(
            "TestDatabase.java",
            """
            package test;
            import com.dbflow5.annotation.Database;
            import com.dbflow5.config.DBFlowDatabase;


            @Database(version = 1)
            public abstract class TestDatabase extends DBFlowDatabase {
            }
            
            """.trimIndent()
        )

        @Language("java")
        val databaseModelSource = SourceFile.java(
            "DatabaseModel.java",
            """
                package test;
                import com.dbflow5.annotation.PrimaryKey;
                import org.jetbrains.annotations.NotNull;

                public class DatabaseModel {
                    @NotNull
                    @PrimaryKey
                    private Integer id;
    
                    public Integer getId() {
                        return id;
                    }
    
                    public void setId(Integer id) {
                        this.id = id;
                    }
                }
            """.trimIndent()
        )

        @Language("java")
        val exampleModel = SourceFile.java(
            "ExampleModel.java",
            """
                 
            package test;
            import com.dbflow5.annotation.Table;
            import com.dbflow5.annotation.ForeignKey;
            import com.dbflow5.annotation.Column;
            import test.TestDatabase;
            import test.DatabaseModel;
            
            @Table(database = TestDatabase.class)
            public class ExampleModel extends DatabaseModel {
                @Column
                String name;
            
                @ForeignKey
                JavaModel model;
            }
            """.trimIndent()
        )

        @Language("java")
        val otherPackageSource = SourceFile.java(
            "JavaModel.java",
            """
            package test;
            
            import com.dbflow5.annotation.Table;
            import test.TestDatabase;
            import com.dbflow5.annotation.PrimaryKey;

            @Table(database = TestDatabase.class)
            public class JavaModel {

                @PrimaryKey
                String id;
            }

            """.trimIndent()
        )
        assertRun(
            sources = listOf(
                source,
                otherPackageSource,
                databaseModelSource,
                exampleModel,
            )
        )
    }


    @Test
    fun `ignore delegate`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey

            @Table(database = TestDatabase::class)
            data class SimpleModel(@PrimaryKey val name: String)

            @Table(database = TestDatabase::class)
            data class SimpleRXModel(@PrimaryKey val name: String)
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }

    @Test
    fun `validate throws error on extra property out of constructor`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "primary.kt",
            """
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table

                @Table(database = TestDatabase::class)
                data class SimplySimple(
                    @PrimaryKey var name: String
                ) {

                    var wellHello: String = ""
                }
            """.trimIndent()
        )
        assertRun(
            sources = listOf(dbFile, source),
            exitCode = KotlinCompilation.ExitCode.COMPILATION_ERROR
        )
    }
}
