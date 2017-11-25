package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.sql.language.OrderBy.Companion.fromNameAlias
import com.raizlabs.android.dbflow.sql.queriable.list
import com.raizlabs.android.dbflow.sql.queriable.result
import org.junit.Assert.fail
import org.junit.Test

class WhereTest : BaseUnitTest() {

    @Test
    fun validateBasicWhere() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name'",
                    select from SimpleModel::class where name.`is`("name"))
        }
    }

    @Test
    fun validateComplexQueryWhere() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' OR `id`=1 AND (`id`=0 OR `name`='hi')",
                    select from SimpleModel::class where name.`is`("name") or id.eq(1) and (id.`is`(0) or name.eq("hi")))
        }
    }

    @Test
    fun validateGroupBy() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`",
                    select from SimpleModel::class where name.`is`("name") groupBy name)
        }
    }

    @Test
    fun validateGroupByNameAlias() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`,`id`",
                    (select from SimpleModel::class where name.`is`("name")).groupBy("name".nameAlias, "id".nameAlias))
        }
    }

    @Test
    fun validateGroupByNameProps() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`,`id`",
                    (select from SimpleModel::class where name.`is`("name")).groupBy(name, id))
        }
    }

    @Test
    fun validateHaving() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' HAVING `name` LIKE 'That'",
                    select from SimpleModel::class where name.`is`("name") having name.like("That"))
        }
    }

    @Test
    fun validateLimit() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' LIMIT 10",
                    select from SimpleModel::class where name.`is`("name") limit 10)
        }
    }

    @Test
    fun validateOffset() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` WHERE `name`='name' OFFSET 10",
                    select from SimpleModel::class where name.`is`("name") offset 10)
        }
    }

    @Test
    fun validateWhereExists() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` " +
                    "WHERE EXISTS (SELECT `name` FROM `SimpleModel` WHERE `name` LIKE 'Andrew')",
                    select from SimpleModel::class
                            whereExists (select(name) from SimpleModel::class where name.like("Andrew")))
        }
    }

    @Test
    fun validateOrderByWhere() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` " +
                    "WHERE `name`='name' ORDER BY `name` ASC",
                    (select from SimpleModel::class
                            where name.eq("name")).orderBy(name, true))
        }
    }

    @Test
    fun validateOrderByWhereAlias() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` " +
                    "WHERE `name`='name' ORDER BY `name` ASC",
                    (select from SimpleModel::class
                            where name.eq("name")).orderBy("name".nameAlias, true))
        }
    }

    @Test
    fun validateOrderBy() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` " +
                    "WHERE `name`='name' ORDER BY `name` ASC",
                    select from SimpleModel::class
                            where name.eq("name") orderBy fromNameAlias("name".nameAlias).ascending())
        }
    }

    @Test
    fun validateOrderByAll() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `TwoColumnModel` " +
                    "WHERE `name`='name' ORDER BY `name` ASC,`id` DESC",
                    (select from TwoColumnModel::class
                            where name.eq("name"))
                            .orderByAll(listOf(
                                    fromNameAlias("name".nameAlias).ascending(),
                                    fromNameAlias("id".nameAlias).descending())))
        }
    }

    @Test
    fun validateNonSelectThrowError() {
        databaseForTable<SimpleModel> {
            try {
                update<SimpleModel>().set(name.`is`("name")).result
                fail("Non select passed")
            } catch (i: IllegalArgumentException) {
                // expected
            }

            try {
                update<SimpleModel>().set(name.`is`("name")).list
                fail("Non select passed")
            } catch (i: IllegalArgumentException) {
                // expected
            }
        }
    }
}
