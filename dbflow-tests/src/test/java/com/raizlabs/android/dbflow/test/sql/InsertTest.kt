package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Insert
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Description:
 */
class InsertTest : FlowTestCase() {

    @Test
    fun testInsert() {

        Delete.table(InsertModel::class.java)

        var insert = SQLite.insert(InsertModel::class.java).orFail()
                .columnValues(InsertModel_Table.name.eq("Test"), InsertModel_Table.value.eq("Test1"))

        assertEquals("INSERT OR FAIL INTO `InsertModel`(`name`, `value`) VALUES('Test','Test1')", insert.query)

        FlowManager.getWritableDatabase(TestDatabase::class.java).execSQL(insert.query)

        var model: InsertModel = Select().from(InsertModel::class.java)
                .where(InsertModel_Table.name.`is`("Test")).querySingle()
        assertNotNull(model)


        insert = SQLite.insert(InsertModel::class.java).orAbort()
                .values("Test2", "Test3")
        assertEquals("INSERT OR ABORT INTO `InsertModel` VALUES('Test2','Test3')", insert.query)

        FlowManager.getWritableDatabase(TestDatabase.NAME).execSQL(insert.query)


        model = Select().from(InsertModel::class.java)
                .where(InsertModel_Table.name.`is`("Test3")).querySingle()
        assertNotNull(model)
    }

    @Test
    fun testInsertMultipleValues() {
        Delete.table(InsertModel::class.java)

        var insert = SQLite.insert(InsertModel::class.java).orFail()
                .columnValues(InsertModel_Table.name.eq("Test"), InsertModel_Table.value.eq("Test1"))
                .columnValues(InsertModel_Table.name.eq("Test2"), InsertModel_Table.value.eq("Test3"))

        assertEquals("INSERT OR FAIL INTO `InsertModel`(`name`, `value`) VALUES('Test','Test1'),('Test2','Test3')", insert.query)

        FlowManager.getWritableDatabase(TestDatabase::class.java).execSQL(insert.query)

        var model: InsertModel = Select().from(InsertModel::class.java)
                .where(InsertModel_Table.name.`is`("Test")).querySingle()
        assertNotNull(model)

        insert = SQLite.insert(InsertModel::class.java).orAbort()
                .values("Test2", "Test3")
                .values("Test4", "Test5")
        assertEquals("INSERT OR ABORT INTO `InsertModel` VALUES('Test2','Test3'),('Test4','Test5')", insert.query)

        FlowManager.getWritableDatabase(TestDatabase.NAME).execSQL(insert.query)

        model = Select().from(InsertModel::class.java)
                .where(InsertModel_Table.name.`is`("Test3")).querySingle()
        assertNotNull(model)
    }

    @Test
    fun testInsertAutoIncNotFirst() {
        Delete.table(InsertModelAutoIncPrimaryKeyNotFirst::class.java)

        var model = InsertModelAutoIncPrimaryKeyNotFirst()
        model.value1 = "test1"
        model.value2 = "test2"

        model.save()

        model = Select().from(InsertModelAutoIncPrimaryKeyNotFirst::class.java)
                .where(InsertModelAutoIncPrimaryKeyNotFirst_Table.value1.`is`("test1")).querySingle()
        assertNotNull(model)

    }

    @Table(database = TestDatabase::class)
    class InsertModelAutoIncPrimaryKeyNotFirst : BaseModel() {
        @Column
        var value1: String? = null

        @Column
        @PrimaryKey(autoincrement = true)
        var id: Int = 0

        @Column
        var value2: String? = null
    }
}
