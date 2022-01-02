package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.dbFile
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Description:
 */
class ManyToManyTests : BaseCompileTest() {
    @Test
    fun `basic many to many`() {
        @Language("kotlin")
        val source = SourceFile.kotlin(
            "ManyToMany.kt",
            """
            package test
            import com.dbflow5.annotation.Column
            import com.dbflow5.annotation.ManyToMany
            import com.dbflow5.annotation.PrimaryKey
            import com.dbflow5.annotation.Table
            
            @ManyToMany(referencedTable = Song::class, generateBaseModel = true)
            @Table(database = TestDatabase::class)
            class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
                         @Column var name: String = "")
            
            @Table(database = TestDatabase::class)
            class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
                       @Column var name: String = "") 
            """.trimIndent()
        )
        assertRun(sources = listOf(dbFile, source))
    }
}