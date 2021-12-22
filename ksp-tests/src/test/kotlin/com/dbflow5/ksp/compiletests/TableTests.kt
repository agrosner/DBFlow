package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class TableTests {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `basic table`() {
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Database
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.config.DBFlowDatabase


            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: String)  
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `foreign key table`() {
        val source = SourceFile.kotlin(
            "ForeignKeyTable.kt",
            """
            package test
            import com.dbflow5.annotation.Database
            import com.dbflow5.annotation.ForeignKey
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.config.DBFlowDatabase

            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: String)
              
            @Table(database = TestDatabase::class)
            class ForeignKeyModel(
                @PrimaryKey val name: String,
                @ForeignKey val model: SimpleModel,
            )

            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `inline class table`() {
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Database
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.config.DBFlowDatabase


            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @JvmInline
            value class Name(val value: String)
            
            @Table(database = TestDatabase::class)
            class SimpleModel(@PrimaryKey val name: Name)  
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `property has global type converter`() {
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Database
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.config.DBFlowDatabase
            import com.dbflow5.data.Blob


            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @Table(database = TestDatabase::class)
            class SimpleModel(
                @PrimaryKey val name: String,
                val blob: Blob,
            )  
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `java class with constructor should apply constructor`() {
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
        val databaseModelSource = SourceFile.java(
            "DatabaseModel.java",
            """
                package test;
                import com.dbflow5.annotation.PrimaryKey;
                import com.dbflow5.structure.BaseModel;

                public class DatabaseModel extends BaseModel {
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
        val exampleModel = SourceFile.java(
            "ExampleModel.java",
            """
                 
            package test;
            import com.dbflow5.annotation.Table;
            import com.dbflow5.annotation.ForeignKey;
            import com.dbflow5.annotation.Column;
            import test.TestDatabase;
            import test.DatabaseModel;
            import test.inner.JavaModel;
            
            @Table(database = TestDatabase.class)
            public class ExampleModel extends DatabaseModel {
                @Column
                String name;
            
                @ForeignKey
                JavaModel model;
            }
            """.trimIndent()
        )
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
        val result = compilation(
            temporaryFolder,
            sources = listOf(
                source,
                otherPackageSource,
                databaseModelSource,
                exampleModel,
            )
        ).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }


    @Test
    fun `ignore delegate`() {
        val source = SourceFile.kotlin(
            "SimpleModel.kt",
            """
            package test
            import com.dbflow5.annotation.Database
            import com.dbflow5.annotation.Table
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.config.DBFlowDatabase
            import com.dbflow5.reactivestreams.structure.BaseRXModel 
            import com.dbflow5.structure.BaseModel


            @Database(version = 1)
            abstract class TestDatabase: DBFlowDatabase()
            
            @Table(database = TestDatabase::class)
            data class SimpleModel(@PrimaryKey val name: String)  : BaseModel()

            @Table(database = TestDatabase::class)
            data class SimpleRXModel(@PrimaryKey val name: String): BaseRXModel()
            """.trimIndent()
        )
        val result = compilation(temporaryFolder, sources = listOf(source)).compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}