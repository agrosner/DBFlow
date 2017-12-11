package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.models.TwoColumnModel
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.query.OrderBy.Companion.fromNameAlias
import com.raizlabs.dbflow5.query.Where
import com.raizlabs.dbflow5.query.groupBy
import com.raizlabs.dbflow5.query.having
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.nameAlias
import com.raizlabs.dbflow5.query.or
import com.raizlabs.dbflow5.query.result
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.query.update
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class WhereTest : BaseUnitTest() {

    @Test
    fun validateBasicWhere() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name")
            "SELECT * FROM `SimpleModel` WHERE `name`='name'".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateComplexQueryWhere() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name") or id.eq(1) and (id.`is`(0) or name.eq("hi"))
            "SELECT * FROM `SimpleModel` WHERE `name`='name' OR `id`=1 AND (`id`=0 OR `name`='hi')".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateGroupBy() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name") groupBy name
            "SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`".assertEquals(query)
            assertCanCopyQuery(query)

        }
    }

    @Test
    fun validateGroupByNameAlias() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class where name.`is`("name")).groupBy("name".nameAlias, "id".nameAlias)
            "SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`,`id`".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateGroupByNameProps() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class where name.`is`("name")).groupBy(name, id)
            "SELECT * FROM `SimpleModel` WHERE `name`='name' GROUP BY `name`,`id`".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateHaving() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name") having name.like("That")
            "SELECT * FROM `SimpleModel` WHERE `name`='name' HAVING `name` LIKE 'That'".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateLimit() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name") limit 10
            "SELECT * FROM `SimpleModel` WHERE `name`='name' LIMIT 10".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateOffset() {
        databaseForTable<SimpleModel> {
            val query = select from SimpleModel::class where name.`is`("name") offset 10
            "SELECT * FROM `SimpleModel` WHERE `name`='name' OFFSET 10".assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateWhereExists() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class
                whereExists (select(name) from SimpleModel::class where name.like("Andrew")))
            ("SELECT * FROM `SimpleModel` " +
                "WHERE EXISTS (SELECT `name` FROM `SimpleModel` WHERE `name` LIKE 'Andrew')").assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateOrderByWhere() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class
                where name.eq("name")).orderBy(name, true)
            ("SELECT * FROM `SimpleModel` WHERE `name`='name' ORDER BY `name` ASC").assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateOrderByWhereAlias() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class
                where name.eq("name")).orderBy("name".nameAlias, true)
            ("SELECT * FROM `SimpleModel` " +
                "WHERE `name`='name' ORDER BY `name` ASC").assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    @Test
    fun validateOrderBy() {
        databaseForTable<SimpleModel> {
            val query = (select from SimpleModel::class
                where name.eq("name") orderBy fromNameAlias("name".nameAlias).ascending())
            ("SELECT * FROM `SimpleModel` " +
                "WHERE `name`='name' ORDER BY `name` ASC").assertEquals(query)
            assertCanCopyQuery(query)
        }
    }

    private fun <T : Any> assertCanCopyQuery(query: Where<T>) {
        val actual = query.cloneSelf()
        query.assertEquals(actual)
        assertTrue(actual !== query)
    }

    @Test
    fun validateOrderByAll() {
        databaseForTable<SimpleModel> {
            val query = (select from TwoColumnModel::class
                where name.eq("name"))
                .orderByAll(listOf(
                    fromNameAlias("name".nameAlias).ascending(),
                    fromNameAlias("id".nameAlias).descending()))
            ("SELECT * FROM `TwoColumnModel` " +
                "WHERE `name`='name' ORDER BY `name` ASC,`id` DESC").assertEquals(query)
            assertCanCopyQuery(query)
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
