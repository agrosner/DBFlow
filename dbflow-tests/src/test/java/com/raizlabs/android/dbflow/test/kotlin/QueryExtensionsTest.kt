package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.language.Join.JoinType.INNER
import com.raizlabs.android.dbflow.sql.language.Join.JoinType.LEFT_OUTER
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.sql.TestModel3
import com.raizlabs.android.dbflow.test.sql.TestModel3_Table
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
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

        var query = select(TestModel1_Table.name) {
            from<TestModel1> {
                where { TestModel1_Table.name.eq("test") }
                    .and { TestModel1_Table.name.isNot("something") }
            }
        }.query

        assertEquals(query.trim(), "SELECT `name` FROM `TestModel1` WHERE `name`='test' AND `name`!='something'")

        var another = select {
            from<TestModel1> {
                join<TestModel1, TestModel2>(INNER) {
                    on { TestModel2_Table.name.withTable().eq(TestModel1_Table.name.withTable()) }
                }

                join<TestModel1, TestModel3>(LEFT_OUTER) {
                    on { TestModel1_Table.name.withTable().eq(TestModel3_Table.name.withTable()) }
                }
            }
        }

        assertEquals(another.query.trim(), "SELECT * FROM `TestModel1` " +
            "INNER JOIN `TestModel2` ON `TestModel2`.`name`=`TestModel1`.`name`  " +
            "LEFT OUTER JOIN `TestModel32` ON `TestModel1`.`name`=`TestModel32`.`name`")
    }

    @Test
    @Throws(Exception::class)
    fun test_updateBuilders() {

        var query = update<TestModel1> {
            set {
                conditions(TestModel1_Table.name.`is`("yes"))
                where { TestModel1_Table.name.eq("no") }
                    .and { TestModel1_Table.name.eq("maybe") }
            }
        }

        assertEquals(query.query.trim(), "UPDATE `TestModel1` SET `name`='yes' WHERE `name`='no' AND `name`='maybe'")
    }

    @Test
    @Throws(Exception::class)
    fun test_deleteBuilders() {

        var query = delete<TestModel1> {
            where {
                TestModel1_Table.name.eq("test")
            }
        }

    }
}


