package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.sql.language.ConditionGroup
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Author: andrewgrosner
 * Description: Test our [ConditionGroup] and
 * [Condition] classes to ensure they generate what they're supposed to.
 */
class BuilderTest : FlowTestCase() {

    /**
     * This test will ensure that all column values are converted appropriately
     */
    @Test
    fun testConditions() {
        var conditionGroup = ConditionGroup.clause()
                .and(ConditionModel_Table.number.`is`(5L))
                .and(ConditionModel_Table.bytes.`is`(5))
                .and(ConditionModel_Table.fraction.`is`(6.5))

        assertEquals("`number`=5 AND `bytes`=5 AND `fraction`=6.5", conditionGroup.query)


        conditionGroup = ConditionGroup.clause()
                .and(ConditionModel_Table.number.between(5L).and(10L))
        assertEquals("`number` BETWEEN 5 AND 10", conditionGroup.query.trim { it <= ' ' })
    }

    @Test
    fun testCollate() {
        val collate = ConditionModel_Table.name.`is`("James").collate(Collate.NOCASE)
        val conditionQueryBuilder = ConditionGroup.clause().and(collate)
        assertEquals("`name`='James' COLLATE NOCASE", conditionQueryBuilder.query.trim { it <= ' ' })

    }

    @Test
    fun testChainingConditions() {
        val conditionQueryBuilder = ConditionGroup.clause()
        conditionQueryBuilder.and(ConditionModel_Table.name.`is`("James"))
                .or(ConditionModel_Table.number.`is`(6L))
                .and(ConditionModel_Table.fraction.`is`(4.5))
        assertEquals("`name`='James' OR `number`=6 AND `fraction`=4.5", conditionQueryBuilder.query.trim { it <= ' ' })
    }

    @Test
    fun testIsOperators() {
        val conditionQueryBuilder = ConditionGroup.clause()
                .and(ConditionModel_Table.name.`is`("James"))
                .or(ConditionModel_Table.fraction.isNotNull)
        assertEquals("`name`='James' OR `fraction` IS NOT NULL", conditionQueryBuilder.query.trim { it <= ' ' })
    }

    @Test
    fun testInOperators() {
        val `in` = ConditionModel_Table.name.`in`("Jason", "Ryan", "Michael")
        var conditionQueryBuilder = ConditionGroup.clause().and(`in`)
        assertEquals("`name` IN ('Jason','Ryan','Michael')", conditionQueryBuilder.query.trim { it <= ' ' })

        val notIn = ConditionModel_Table.name.notIn("Jason", "Ryan", "Michael")
        conditionQueryBuilder = ConditionGroup.clause().and(notIn)
        assertEquals("`name` NOT IN ('Jason','Ryan','Michael')", conditionQueryBuilder.query.trim { it <= ' ' })
    }

    @Test
    fun testCombinedOperations() {
        val combinedCondition = ConditionGroup.clause()
                .and(ConditionGroup.clause()
                        .and(PropertyFactory.from(String::class.java, "A").eq(PropertyFactory.from(String::class.java, "B")))
                        .or(PropertyFactory.from(String::class.java, "B").eq(PropertyFactory.from(String::class.java, "C"))))
                .and(PropertyFactory.from(String::class.java, "C").eq("D"))
        assertEquals("(A=B OR B=C) AND C='D'", combinedCondition.query)
    }

    @Test
    fun testCombinedOperationsReverse() {
        val combinedCondition = ConditionGroup.clause()
                .and(PropertyFactory.from(String::class.java, "C").eq("D"))
                .and(ConditionGroup.clause()
                        .and(PropertyFactory.from(String::class.java, "A")
                                .eq(PropertyFactory.from(String::class.java, "B")))
                        .or(PropertyFactory.from(String::class.java, "B")
                                .eq(PropertyFactory.from(String::class.java, "C"))))
        assertEquals("C='D' AND (A=B OR B=C)", combinedCondition.query)

        val query = SQLite.select().from(TestModel1::class.java)
                .where(combinedCondition)
                .query
        assertEquals("SELECT * FROM `TestModel1` WHERE (C='D' AND (A=B OR B=C))", query.trim { it <= ' ' })
    }

}

