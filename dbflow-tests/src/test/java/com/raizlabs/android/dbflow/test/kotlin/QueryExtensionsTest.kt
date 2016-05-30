package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.sql.TestModel3_Table
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table.name
import com.raizlabs.android.dbflow.test.structure.TestModel2
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class QueryExtensionsTest : FlowTestCase() {

    @Test
    @Throws(Exception::class)
    fun test_canEquals() {

        var query = (SQLite.select(name) from TestModel1::class where
                (name `is` "test") and
                (name isNot "something")).query

        assertEquals(query.trim(), "SELECT `name` FROM `TestModel1` WHERE `name`='test' AND `name`!='something'")

        var another = select from TestModel1::class innerJoin
                TestModel2::class on
                TestModel2_Table.name.withTable().eq(name.withTable()) leftOuterJoin
                TestModel1::class on name.withTable().eq(TestModel3_Table.name.withTable())

        assertEquals(another.query.trim(), "SELECT * FROM `TestModel1` " +
                "INNER JOIN `TestModel2` ON `TestModel2`.`name`=`TestModel1`.`name`  " +
                "LEFT OUTER JOIN `TestModel32` ON `TestModel1`.`name`=`TestModel32`.`name`")
    }

    @Test
    @Throws(Exception::class)
    fun test_updateBuilders() {

        var query = update(TestModel1::class) set
                (name `is` "yes") where
                (name eq "no") and
                (name eq "maybe")

        assertEquals(query.query.trim(), "UPDATE `TestModel1` SET `name`='yes' WHERE `name`='no' AND `name`='maybe'")
    }

    @Test
    @Throws(Exception::class)
    fun test_deleteBuilders() {
        var query = delete(TestModel1::class) where
                name.eq("test")

    }

    @Test
    @Throws(Exception::class)
    fun test_insertBuilders() {

        var query = insert(TestModel1::class) orReplace {
            into<TestModel1>(KotlinTestModel_Table.id to 5, KotlinTestModel_Table.name to "5")
        }

    }
}


