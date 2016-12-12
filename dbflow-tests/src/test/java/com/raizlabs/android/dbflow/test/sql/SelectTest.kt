package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.From
import com.raizlabs.android.dbflow.sql.language.Join
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.language.Where
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import com.raizlabs.android.dbflow.test.structure.TestModel2
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table

import org.junit.Test

import com.raizlabs.android.dbflow.test.sql.TestModel3_Table.name
import com.raizlabs.android.dbflow.test.sql.TestModel3_Table.type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class SelectTest : FlowTestCase() {

    @Test
    fun test_simpleSelectStatement() {
        val where = Select(name).from(TestModel1::class.java)
                .where(name.`is`("test"))

        assertEquals("SELECT `name` FROM `TestModel1` WHERE `name`='test'", where.query.trim { it <= ' ' })
        where.query()

    }

    @Test
    fun test_simpleSelectStatement2() {
        val where4 = Select().from(TestModel3::class.java)
                .where(name.eq("test"))
                .and(type.`is`("test"))

        assertEquals("SELECT * FROM `TestModel32` WHERE `name`='test' AND `type`='test'",
                where4.query.trim { it <= ' ' })
    }

    @Test
    fun test_multipleProjectionAndSelection() {
        val where1 = Select(name, type).from(TestModel3::class.java)
                .where(name.`is`("test"),
                        type.`is`("test"))

        assertEquals("SELECT `name`,`type` FROM `TestModel32` WHERE `name`='test' AND `type`='test'", where1.query.trim { it <= ' ' })
    }

    @Test
    fun test_distinctClause() {
        val where2 = Select().distinct().from(TestModel3::class.java).where()

        assertEquals("SELECT DISTINCT * FROM `TestModel32`", where2.query.trim { it <= ' ' })
        where2.query()
    }

    @Test
    fun test_countClause() {
        val where3 = Select(Method.count()).from(TestModel3::class.java).where()

        assertEquals("SELECT COUNT(*) FROM `TestModel32`", where3.query.trim { it <= ' ' })
        where3.query()

        val where6 = Select(Method.count(type))
                .from(TestModel3::class.java)
                .orderBy(name, true)
                .orderBy(type, true)
        assertEquals("SELECT COUNT(`type`) FROM `TestModel32` ORDER BY `name` ASC,`type` ASC", where6.query.trim { it <= ' ' })

    }

    @Test
    fun test_maxSelect() {
        val methodQuery = SQLite.select(Method.max(TestModel3_Table.type).`as`("troop"))
                .from(TestModel3::class.java).query
        assertEquals("SELECT MAX(`type`) AS `troop` FROM `TestModel32`", methodQuery.trim { it <= ' ' })
    }

    @Test
    fun test_nestedSelect() {
        val query = SQLite.select()
                .from(TestModel3::class.java)
                .where(TestModel3_Table.type
                        .`in`(SQLite.select(TestModel2_Table.name)
                                .from(TestModel2::class.java)
                                .where(TestModel2_Table.name.`is`("Test")))).query
        assertEquals("SELECT * FROM `TestModel32` WHERE `type` IN " + "(SELECT `name` FROM `TestModel2` WHERE `name`='Test' )", query.trim { it <= ' ' })
    }


    @Test
    fun test_complicatedSum() {
        val operationalQuery = SQLite.select(Method(Method.sum(TestModel3_Table.name))
                .minus(Method.sum(TestModel3_Table.type)).`as`("troop"), TestModel3_Table.type)
                .from(TestModel3::class.java).query

        assertEquals("SELECT (SUM(`name`) - SUM(`type`)) AS `troop`,`type` FROM `TestModel32`", operationalQuery.trim { it <= ' ' })
    }

    @Test
    fun test_withTableAs() {
        val query = SQLite.select(TestModel1_Table.name.withTable().`as`("program_id")).query
        assertEquals("SELECT `TestModel1`.`name` AS `program_id`", query.trim { it <= ' ' })
    }

    @Test
    fun test_joins() {

        val testModel1 = TestModel1()
        testModel1.name = "Test"
        testModel1.save()

        val testModel2 = TestModel3()
        testModel2.name = "Test"
        testModel2.save()

        val baseFrom = Select().from(TestModel1::class.java)
        baseFrom.join(TestModel3::class.java, Join.JoinType.CROSS).on(TestModel1_Table.name.withTable().eq(TestModel3_Table.name.withTable()))

        assertEquals("SELECT * FROM `TestModel1` CROSS JOIN `TestModel32` ON `TestModel1`.`name`=`TestModel32`.`name`", baseFrom.query.trim { it <= ' ' })

        val list = baseFrom.where().queryList()
        assertTrue(!list.isEmpty())

        val where = Select().from(TestModel1::class.java).join(TestModel3::class.java, Join.JoinType.INNER).natural().where()
        assertEquals("SELECT * FROM `TestModel1` NATURAL INNER JOIN `TestModel32`", where.query.trim { it <= ' ' })

        where.query()
    }

    @Test
    fun test_nulls() {

        val nullable: String? = null
        val query = SQLite.select().from(TestModel1::class.java).where(TestModel1_Table.name.eq(nullable)).query
        assertEquals("SELECT * FROM `TestModel1` WHERE `name`=NULL", query.trim { it <= ' ' })
    }


    @Test
    fun test_selectProjectionQuery() {
        val where = SQLite.select(TestModel1_Table.name)
                .from(TestModel1::class.java).`as`("TableName")
                .where(TestModel1_Table.name.eq("Test"))

        val query = SQLite.select(PropertyFactory.from(where), TestModel1_Table.name)
                .from(TestModel1::class.java).query

        assertEquals("SELECT (SELECT `name` FROM `TestModel1` AS `TableName`" + " WHERE `name`='Test'),`name` FROM `TestModel1`", query.trim { it <= ' ' })


    }
}
