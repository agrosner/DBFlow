package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.insert
import com.dbflow5.query.property.propertyString
import com.dbflow5.query.property.tableName
import com.dbflow5.query.select
import com.dbflow5.structure.save
import org.junit.Test

/**
 * Description:
 */
class Fts4ModelTest : BaseUnitTest() {

    @Test
    fun validate_fts4_created() {
        database<TestDatabase> { db ->
            val model = Fts4Model(name = "FTSBABY")
            model.save(db)

            val rows = (insert<Fts4VirtualModel2>(
                propertyString<Any>("docid"),
                Fts4VirtualModel2_Table.name) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from Fts4Model::class))
                .executeInsert(db)
            assert(rows > 0)


        }
    }

    @Test
    fun match_query() {
        validate_fts4_created()
        database<TestDatabase> { db ->
            val model = (select from Fts4VirtualModel2::class where (tableName<Fts4VirtualModel2>() match "FTSBABY"))
                .querySingle(db)
            assert(model != null)
        }
    }
}