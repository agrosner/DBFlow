package com.dbflow5.ksp.compiletests

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Description:
 */
class DatabaseTests : BaseCompileTest() {

    @Test
    fun `database with inclusive objects`() {
        // has specific tables, views, queries
        @Language("kotlin") val file = SourceFile.kotlin(
            "Inclusive.kt",
            """
        import com.dbflow5.annotation.Database
        import com.dbflow5.annotation.Migration
        import com.dbflow5.annotation.ModelView
        import com.dbflow5.annotation.ModelViewQuery
        import com.dbflow5.annotation.PrimaryKey
        import com.dbflow5.annotation.Query
        import com.dbflow5.annotation.Table
        import com.dbflow5.config.DBFlowDatabase
        import com.dbflow5.database.DatabaseWrapper
        import com.dbflow5.migration.BaseMigration
        import com.dbflow5.query.select

        @Table
        data class SimpleModel(
            @PrimaryKey val name: String,
        )

        @ModelView
        data class SimpleView(
            val name: String
        ) {
            companion object {
                @ModelViewQuery
                val query = (select from SimpleModel::class)
            }
        }

        @Query
        data class SimpleQuery(
            val firstName: String,
            val lastName: String,
        )

        @Database(
            version = 1,
            tables = [
                SimpleModel::class,
            ],
            views = [
                SimpleView::class,
            ],
            queries = [
                SimpleQuery::class,
            ],
            migrations = [
                TestDatabase.SomeMigration::class,
            ],
        )
        abstract class TestDatabase: DBFlowDatabase() {
            @Migration(version = 2)
            class SomeMigration: BaseMigration() {
                override fun migrate(database: DatabaseWrapper){
                    // do nothing here.            
                }
            }
        }
        
        """.trimIndent()
        )

        assertRun(
            sources = listOf(file)
        ) {

        }
    }
}