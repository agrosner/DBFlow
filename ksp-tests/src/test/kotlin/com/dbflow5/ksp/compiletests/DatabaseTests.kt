package com.dbflow5.ksp.compiletests

import com.dbflow5.ksp.compiletests.sourcefiles.Source
import org.intellij.lang.annotations.Language
import kotlin.test.Test

/**
 * Description:
 */
class DatabaseTests : BaseCompileTest() {

    @Test
    fun `database with inclusive objects`() {
        // has specific tables, views, queries
        @Language("kotlin") val file = Source.KotlinSource(
            "Inclusive",
            """
        import com.dbflow5.adapter.ModelAdapter
        import com.dbflow5.adapter.ViewAdapter
        import com.dbflow5.adapter.QueryAdapter
        import com.dbflow5.annotation.Database
        import com.dbflow5.annotation.Migration
        import com.dbflow5.annotation.ModelView
        import com.dbflow5.annotation.PrimaryKey
        import com.dbflow5.annotation.Query
        import com.dbflow5.annotation.Table
        import com.dbflow5.database.DBFlowDatabase
        import com.dbflow5.database.DatabaseWrapper
        import com.dbflow5.database.scope.MigrationScope
        import com.dbflow5.database.migration.Migration
        import com.dbflow5.query.select

        @Table
        data class SimpleModel(
            @PrimaryKey val name: String,
        )

        @ModelView("SELECT * FROM `SimpleModel`")
        data class SimpleView(
            val name: String
        ) 

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
        
            abstract val simpleModelAdapter: ModelAdapter<SimpleModel>
            abstract val simpleViewAdapter: ViewAdapter<SimpleView>
            abstract val simpleQueryAdapter: QueryAdapter<SimpleQuery>

            @Migration(version = 2)
            class SomeMigration: Migration {
                override suspend fun MigrationScope.migrate(database: DatabaseWrapper){
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