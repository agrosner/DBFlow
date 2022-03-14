package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.Source
import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import kotlin.test.Test

/**
 * Description:
 */
class OneToManyTests : BaseCompileTest() {

    @Test
    fun `basic one to many`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.OneToMany",
            """
                package test
                import com.dbflow5.annotation.Column
                import com.dbflow5.annotation.ForeignKey
                import com.dbflow5.annotation.OneToManyRelation
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table
                
                @OneToManyRelation(childTable = Song::class)
                @Table(database = TestDatabase::class)
                class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
                             @Column var name: String = "")
                
                @Table(database = TestDatabase::class)
                class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
                           @ForeignKey(tableClass = Artist::class)
                           var artistId: Int = 0) 
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }

    @Test
    fun `verify one to many can be used as db adapter`() {
        @Language("kotlin")
        val source = Source.KotlinSource(
            "test.OneToMany",
            """
                package test
                import com.dbflow5.adapter.ModelAdapter
                import com.dbflow5.adapter.QueryAdapter
                import com.dbflow5.annotation.Column
                import com.dbflow5.annotation.Database
                import com.dbflow5.annotation.ForeignKey
                import com.dbflow5.annotation.OneToManyRelation
                import com.dbflow5.annotation.PrimaryKey
                import com.dbflow5.annotation.Table
                import com.dbflow5.database.DBFlowDatabase
                
                @OneToManyRelation(childTable = Song::class)
                @Table
                class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
                             @Column var name: String = "")
                
                @Table
                class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
                           @ForeignKey(tableClass = Artist::class)
                           var artistId: Int = 0) 
        
                @Database(
                    tables = [
                       Artist::class,
                       Song::class,
                    ],
                    version = 1,
                )
                abstract class TestDatabase: DBFlowDatabase {
                    abstract val artistSongAdapter: QueryAdapter<Artist_Song>
                }

            """.trimIndent()
        )
        assertRun(sources = listOf(source))
    }
}