package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.indexOn
import com.raizlabs.dbflow5.query.nameAlias
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest : BaseUnitTest() {

    @Test
    fun validateBasicIndex() {
        databaseForTable<SimpleModel> {
            assertEquals("CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
                    indexOn<SimpleModel>("index", SimpleModel_Table.name).query)
        }
    }

    @Test
    fun validateUniqueIndex() {
        databaseForTable<SimpleModel> {
            assertEquals("CREATE UNIQUE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
                    indexOn<SimpleModel>("index").unique(true).and(SimpleModel_Table.name)
                            .and("test".nameAlias).query)
        }
    }

    @Test
    fun validateBasicIndexNameAlias() {
        databaseForTable<SimpleModel> {
            assertEquals("CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
                    indexOn<SimpleModel>("index", "name".nameAlias, "test".nameAlias).query)
        }
    }
}