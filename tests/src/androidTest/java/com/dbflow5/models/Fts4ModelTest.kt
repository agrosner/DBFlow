package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.insert
import com.dbflow5.query.property.propertyString
import com.dbflow5.quote
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

            val rows = insert<Fts4VirtualModel>(propertyString<Any>("Fts4VirtualModel".quote()))
                    .values("rebuild")
                    .executeInsert(db)
            assert(rows > 0)


        }
    }
}