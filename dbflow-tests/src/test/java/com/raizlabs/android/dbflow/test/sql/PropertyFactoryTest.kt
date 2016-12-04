package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Where
import com.raizlabs.android.dbflow.sql.language.property.ByteProperty
import com.raizlabs.android.dbflow.sql.language.property.CharProperty
import com.raizlabs.android.dbflow.sql.language.property.DoubleProperty
import com.raizlabs.android.dbflow.sql.language.property.FloatProperty
import com.raizlabs.android.dbflow.sql.language.property.IntProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import com.raizlabs.android.dbflow.test.structure.TestModel2
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Description:
 */
class PropertyFactoryTest : FlowTestCase() {

    @Test
    fun test_delete() {
        val time = System.currentTimeMillis()

        val delete = SQLite.delete(TestModel2::class.java)
                .where(TestModel2_Table.model_order.plus(PropertyFactory.from(5)).lessThan(time.toInt()))
        assertEquals("DELETE FROM `TestModel2` WHERE `model_order` + 5<" + time.toInt(), delete.query.trim { it <= ' ' })
    }

    @Test
    fun test_charProperty() {
        val charProperty = PropertyFactory.from('c')
        assertEquals("'c'", charProperty.query)
        val queryBuilder = QueryBuilder()
        charProperty.between('d').and('e').appendConditionToQuery(queryBuilder)
        assertEquals("'c' BETWEEN 'd' AND 'e'", queryBuilder.getQuery().trim({ it <= ' ' }))
    }

    @Test
    fun test_stringProperty() {
        val stringProperty = PropertyFactory.from("MyGirl")
        assertEquals("'MyGirl'", stringProperty.query)
        val queryBuilder = QueryBuilder()
        stringProperty.concatenate("Talkin' About").appendConditionToQuery(queryBuilder)
        assertEquals("'MyGirl'='MyGirl' || 'Talkin'' About'", queryBuilder.getQuery().trim({ it <= ' ' }))
    }

    @Test
    fun test_byteProperty() {
        val byteProperty = PropertyFactory.from(5.toByte())
        assertEquals("5", byteProperty.query)
        val queryBuilder = QueryBuilder()
        byteProperty.`in`(6.toByte(), 7.toByte(), 8.toByte(), 9.toByte()).appendConditionToQuery(queryBuilder)
        assertEquals("5 IN (6,7,8,9)", queryBuilder.getQuery().trim({ it <= ' ' }))
    }

    @Test
    fun test_intProperty() {
        val intProperty = PropertyFactory.from(5)
        assertEquals("5", intProperty.query)
        val queryBuilder = QueryBuilder()
        intProperty.greaterThan(TestModel2_Table.model_order).appendConditionToQuery(queryBuilder)
        assertEquals("5>`model_order`", queryBuilder.getQuery().trim({ it <= ' ' }))
    }

    @Test
    fun test_doubleProperty() {
        val doubleProperty = PropertyFactory.from(10.0)
        assertEquals("10.0", doubleProperty.query)
        val queryBuilder = QueryBuilder()
        doubleProperty.plus(ConditionModel_Table.fraction).lessThan(ConditionModel_Table.fraction).appendConditionToQuery(queryBuilder)
        assertEquals("10.0 + `fraction`<`fraction`", queryBuilder.getQuery().trim({ it <= ' ' }))
    }

    @Test
    fun test_floatProperty() {
        val floatProperty = PropertyFactory.from(20f)
        assertEquals("20.0", floatProperty.query)
        val queryBuilder = QueryBuilder()
        floatProperty.minus(ConditionModel_Table.floatie).minus(ConditionModel_Table.floatie).eq(5f).appendConditionToQuery(queryBuilder)
    }

    @Test
    fun test_queryProperty() {
        val model1Property = PropertyFactory.from(
                SQLite.select().from(TestModel1::class.java).where(TestModel1_Table.name.eq("Test"))).`as`("Cool")
        assertEquals("(SELECT * FROM `TestModel1` WHERE `name`='Test') AS `Cool`", model1Property.definition)
        val queryBuilder = QueryBuilder()
        model1Property.minus(ConditionModel_Table.fraction).plus(TestModel1_Table.name.withTable()).like("%somethingnotvalid%").appendConditionToQuery(queryBuilder)
        assertEquals("(SELECT * FROM `TestModel1` WHERE `name`='Test') - `fraction` + `TestModel1`.`name` LIKE '%somethingnotvalid%'", queryBuilder.getQuery().trim({ it <= ' ' }))

    }
}
