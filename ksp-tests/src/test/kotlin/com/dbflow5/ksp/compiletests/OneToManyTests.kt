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
class OneToManyTests {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `basic many to many`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "OneToMany.kt",
            """
                import com.dbflow5.annotation.Column
                import com.dbflow5.annotation.Database
                import com.dbflow5.annotation.ForeignKey
                import com.dbflow5.annotation.OneToManyRelation
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table
                import com.dbflow5.config.DBFlowDatabase
                
                @Database(version = 1)
                abstract class TestDatabase: DBFlowDatabase()
                
                @OneToManyRelation(childTable = Song::class)
                @Table(database = TestDatabase::class)
                class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
                             @Column var name: String = "")
                
                @Table(database = TestDatabase::class)
                class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
                           @ForeignKey(tableClass = Artist::class)
                           var id: Int = 0) 
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