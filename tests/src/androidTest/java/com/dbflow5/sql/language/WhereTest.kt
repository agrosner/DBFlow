package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.query.orderBy
import com.dbflow5.query.operations.StandardMethods
import com.dbflow5.query.operations.invoke
import com.dbflow5.query.operations.like
import com.dbflow5.query.operations.literalOf
import com.dbflow5.query.operations.match
import com.dbflow5.query.select
import org.junit.Test

class WhereTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    private val twoColumnModelAdapter
        get() = database<TestDatabase>().twoColumnModelAdapter

    @Test
    fun validateBasicWhere() {
        val query = simpleModelAdapter.select() where (SimpleModel_Table.name eq "name")
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name'".assertEquals(query)
    }

    @Test
    fun validateComplexQueryWhere() {
        val query = (
            twoColumnModelAdapter.select()
                where (TwoColumnModel_Table.name eq "name")
                or (TwoColumnModel_Table.id eq 1)
                and (
                TwoColumnModel_Table.id eq 0
                    or (TwoColumnModel_Table.name eq "hi")))
        ("SELECT * FROM `TwoColumnModel` " +
            "WHERE `name` = 'name' " +
            "OR `id` = 1 " +
            "AND (`id` = 0 OR `name` = 'hi')")
            .assertEquals(query)
    }

    @Test
    fun validateGroupBy() {
        val query = (
            simpleModelAdapter.select()
                where SimpleModel_Table.name.eq("name")
                groupBy SimpleModel_Table.name
            )
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' GROUP BY `name`".assertEquals(query)
    }

    @Test
    fun validateGroupByNameAlias() {
        val query =
            (simpleModelAdapter.select() where SimpleModel_Table.name.eq("name")).groupBy(
                "name".nameAlias,
                "id".nameAlias
            )
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' GROUP BY `name`,`id`".assertEquals(query)
    }

    @Test
    fun validateGroupByNameProps() {
        val query = (
            twoColumnModelAdapter.select()
                where TwoColumnModel_Table.name.eq("name"))
            .groupBy(
                TwoColumnModel_Table.name,
                TwoColumnModel_Table.id
            )
        "SELECT * FROM `TwoColumnModel` WHERE `name` = 'name' GROUP BY `name`,`id`".assertEquals(
            query
        )
    }

    @Test
    fun validateHaving() {
        val query =
            simpleModelAdapter.select() where SimpleModel_Table.name.eq("name") having SimpleModel_Table.name.like(
                "That"
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
                having StandardMethods.Min<Int>().invoke(literalOf("ROWID")).greaterThan(5))
        )
    }

    @Test
    fun validateLimit() {
        val query =
            simpleModelAdapter.select() where SimpleModel_Table.name.eq("name") limit 10
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' LIMIT 10".assertEquals(query)
    }

    @Test
    fun validateOffset() {
        val query = simpleModelAdapter.select() where SimpleModel_Table.name.eq("name") offset 10
        "SELECT * FROM `SimpleModel` WHERE `name` = 'name' OFFSET 10".assertEquals(query)
    }

    @Test
    fun validateWhereExists() {
        val query = (
            simpleModelAdapter.select()
                whereExists (
                simpleModelAdapter.select(SimpleModel_Table.name)
                    where SimpleModel_Table.name.like("Andrew")
                )
            )
        ("SELECT * FROM `SimpleModel` " +
            "WHERE EXISTS (SELECT `name` FROM `SimpleModel` WHERE `name` LIKE 'Andrew')").assertEquals(
            query
        )
    }

    @Test
    fun validateOrderByWhere() {
        val query = (simpleModelAdapter.select()
            where SimpleModel_Table.name.eq("name")).orderBy(SimpleModel_Table.name, true)
        ("SELECT * FROM `SimpleModel` WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderByWhereAlias() {
        val query = (simpleModelAdapter.select()
            where SimpleModel_Table.name.eq("name"))
            .orderBy("name".nameAlias, true)
        ("SELECT * FROM `SimpleModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderBy() {
        val query = (simpleModelAdapter.select()
            where SimpleModel_Table.name.eq("name") orderBy orderBy("name".nameAlias).asc())
        ("SELECT * FROM `SimpleModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC").assertEquals(query)
    }

    @Test
    fun validateOrderByAll() {
        val query = (twoColumnModelAdapter.select()
            where TwoColumnModel_Table.name.eq("name")
            orderByAll listOf(
            orderBy("name".nameAlias).asc(),
            orderBy("id".nameAlias).desc()
        ))
        ("SELECT * FROM `TwoColumnModel` " +
            "WHERE `name` = 'name' ORDER BY `name` ASC,`id` DESC").assertEquals(query)
    }

    @Test
    fun validate_match_operator() {
        val query = (simpleModelAdapter.select() where (SimpleModel_Table.name match "%s"))
        ("SELECT * FROM `SimpleModel` WHERE `name` MATCH '%s'").assertEquals(query)
    }
}
