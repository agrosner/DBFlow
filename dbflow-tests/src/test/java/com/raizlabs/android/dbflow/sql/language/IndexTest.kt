package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
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