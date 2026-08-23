package com.dbflow5.test.sql.language

import com.dbflow5.query.NameAlias
import com.dbflow5.query.methods.min
import com.dbflow5.query.nameAlias
import com.dbflow5.query.operations.like
import com.dbflow5.query.operations.literalOf
import com.dbflow5.query.operations.match
import com.dbflow5.query.orderBy
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.test.assertEquals
import kotlin.test.Test

class WhereTest : TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateBasicWhere() = dbRule {
        val query = simpleModelAdapter.select() where (SimpleModel.name eq "name")
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name'".assertEquals(query)
    }

    @Test
    fun validateComplexQueryWhere() = dbRule {
        val query = (
            twoColumnModelAdapter.select()
                where (TwoColumnModel.name eq "name")
                or (TwoColumnModel.id eq 1)
                and (
                TwoColumnModel.id eq 0
                    or (TwoColumnModel.name eq "hi")))
        ("SELECT * FROM `TwoColumnModel` " +
            "WHERE `name` = 'name' " +
            "OR `id` = 1 " +
            "AND (`id` = 0 OR `name` = 'hi')")
            .assertEquals(query)
    }

    @Test
    fun validateGroupBy() = dbRule {
        val query = (
            simpleModelAdapter.select()
                where SimpleModel.name.eq("name")
                groupBy SimpleModel.name
            )
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' GROUP BY `name`".assertEquals(query)
    }

    @Test
    fun validateGroupByNameAlias() = dbRule {
        val query =
            (simpleModelAdapter.select() where SimpleModel.name.eq("name")).groupBy(
                "name".nameAlias,
                "id".nameAlias
            )
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' GROUP BY `name`,`id`".assertEquals(query)
    }

    @Test
    fun validateGroupByNameProps() = dbRule {
        val query = (
            twoColumnModelAdapter.select()
                where TwoColumnModel.name.eq("name"))
            .groupBy(
                TwoColumnModel.name,
                TwoColumnModel.id
            )
        "SELECT * FROM `TwoColumnModel` WHERE `name` = 'name' GROUP BY `name`,`id`".assertEquals(
            query
        )
    }

    @Test
    fun validateHaving() = dbRule {
        val query = (simpleModelAdapter.select()
            where SimpleModel.name.eq("name")
            having SimpleModel.name.like("That")
            )
        ("SELECT * FROM `SimpleModel` " +
            "WHERE `name` = 'name' " +
            "HAVING `name` LIKE 'That'").assertEquals(
            query
        )

        ("SELECT * FROM `SimpleModel` " +
            "GROUP BY exampleValue " +
            "HAVING MIN(ROWID) > 5").assertEquals(
            (simpleModelAdapter.select()
                groupBy NameAlias.rawBuilder("exampleValue").build()
                having min<Int>()(literalOf("ROWID")).greaterThan(5))
        )
    }

    @Test
    fun validateLimit() = dbRule {
        val query =
            simpleModelAdapter.select() where SimpleModel.name.eq("name") limit 10
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' LIMIT 10".assertEquals(query)
    }

    @Test
    fun validateOffset() = dbRule {
        val query = simpleModelAdapter.select() where SimpleModel.name.eq("name") offset 10
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' OFFSET 10".assertEquals(query)
    }

    @Test
    fun validateWhereExists() = dbRule {
        val query = (
            simpleModelAdapter.select()
                whereExists (
                simpleModelAdapter.select(SimpleModel.name)
                    where SimpleModel.name.like("Andrew")
                )
            )
        ("SELECT * FROM `SimpleModel` " +
            "WHERE EXISTS (SELECT `name` FROM `SimpleModel` WHERE `name` LIKE 'Andrew')").assertEquals(
            query
        )
    }

    @Test
    fun validateOrderByWhere() = dbRule {
        val query = (simpleModelAdapter.select()
            where SimpleModel.name.eq("name")).orderBy(SimpleModel.name, true)
        ("SELECT * FROM `SimpleModel` WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderByWhereAlias() = dbRule {
        val query = (simpleModelAdapter.select()
            where SimpleModel.name.eq("name"))
            .orderBy("name".nameAlias, true)
        ("SELECT * FROM `SimpleModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderBy() = dbRule {
        val query = (simpleModelAdapter.select()
            where SimpleModel.name.eq("name") orderBy orderBy("name".nameAlias).asc())
        ("SELECT * FROM `SimpleModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderByAll() = dbRule {
        val query = (twoColumnModelAdapter.select()
            where TwoColumnModel.name.eq("name")
            orderByAll listOf(
            orderBy("name".nameAlias).asc(),
            orderBy("id".nameAlias).desc()
        ))
        ("SELECT * FROM `TwoColumnModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC,`id` DESC").assertEquals(query)
    }

    @Test
    fun validate_match_operator() = dbRule {
        val query = (simpleModelAdapter.select() where (SimpleModel.name match "%s"))
        ("SELECT * FROM `SimpleModel` WHERE `name` MATCH '%s'").assertEquals(query)
    }
}
