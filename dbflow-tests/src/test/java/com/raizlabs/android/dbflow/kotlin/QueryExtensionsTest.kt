package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.kotlin.Account_Table.id
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.TestModel3
import com.raizlabs.android.dbflow.sql.TestModel3_Table
import com.raizlabs.android.dbflow.sql.language.SQLite.select
import com.raizlabs.android.dbflow.structure.TestModel1
import com.raizlabs.android.dbflow.structure.TestModel1_Table.name
import com.raizlabs.android.dbflow.structure.TestModel2
import com.raizlabs.android.dbflow.structure.TestModel2_Table
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class QueryExtensionsTest : FlowTestCase() {

    @Test
    @Throws(Exception::class)
    fun test_canEquals() {

        val query = ((select(name)
            from KotlinTestModel::class
            where (name `is` "test")
            and (name isNot "something")).query)

        assertEquals(query.trim(), "SELECT `name` FROM `KotlinTestModel` WHERE `name`='test' AND `name`!='something'")

        val another = (select
            from TestModel1::class
            innerJoin TestModel2::class
            on (TestModel2_Table.name.withTable() eq (name.withTable()))
            leftOuterJoin TestModel3::class on (name.withTable() eq TestModel3_Table.name.withTable()))

        assertEquals("SELECT * FROM `TestModel1` " +
            "INNER JOIN `TestModel2` ON `TestModel2`.`name`=`KotlinTestModel`.`name`  " +
            "LEFT OUTER JOIN `TestModel32` ON `KotlinTestModel`.`name`=`TestModel32`.`name`",
            another.query.trim())
    }

    @Test
    @Throws(Exception::class)
    fun test_updateBuilders() {
        val query = (update(TestModel1::class)
            set (name `is` "yes")
            where (name eq "no")
            and (name eq "maybe"))
        assertEquals(query.query.trim(), "UPDATE `TestModel1` SET `name`='yes' WHERE `name`='no' AND `name`='maybe'")
    }

    @Test
    @Throws(Exception::class)
    fun test_deleteBuilders() {
        val query = (delete(TestModel1::class)
            where (name eq "test"))

    }

    @Test
    @Throws(Exception::class)
    fun test_insertBuilders() {

        var query = (insert(TestModel1::class)
            orReplace (into(id to 5, name to "5")))


    }
}



