package com.raizlabs.android.dbflow.test.sql

import android.content.ContentValues
import com.raizlabs.android.dbflow.sql.SqlUtils
import com.raizlabs.android.dbflow.sql.language.ConditionGroup
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Update
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.provider.NoteModel_Table.providerModel
import com.raizlabs.android.dbflow.test.provider.NoteModel_Table.note
import com.raizlabs.android.dbflow.test.provider.TestContentProvider
import com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.*
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import org.junit.Assert.*
import org.junit.Test

class UpdateTest : FlowTestCase() {

    @Test
    fun testUpdateStatement() {
        val update = SQLite.update(TestModel1::class.java)

        // Verify update prefix

        assertUpdateSuffix("ROLLBACK", update.orRollback())
        assertUpdateSuffix("ABORT", update.orAbort())
        assertUpdateSuffix("REPLACE", update.orReplace())
        assertUpdateSuffix("FAIL", update.orFail())
        assertUpdateSuffix("IGNORE", update.orIgnore())

        val from = SQLite.update(TestModel1::class.java)

        assertEquals("UPDATE `TestModel1`", from.query.trim { it <= ' ' })

        val where = from.set(TestModel1_Table.name.`is`("newvalue"))
            .where(TestModel1_Table.name.`is`("oldvalue"))

        assertEquals("UPDATE `TestModel1` SET `name`='newvalue' WHERE `name`='oldvalue'",
            where.query.trim { it <= ' ' })
        where.query()

        var query = SQLite.update(BoxedModel::class.java).set(integerField.concatenate(1)).query
        assertEquals("UPDATE `BoxedModel` SET `integerField`=`integerField` + 1",
            query.trim { it <= ' ' })

        query = SQLite.update(BoxedModel::class.java).set(name.concatenate("Test")).query
        assertEquals("UPDATE `BoxedModel` SET `name`=`name` || 'Test'",
            query.trim { it <= ' ' })

        query = SQLite.update(BoxedModel::class.java).set(name.eq("Test"))
            .where(name.eq(name.withTable()))
            .query
        assertEquals("UPDATE `BoxedModel` SET `name`='Test' WHERE `name`=`BoxedModel`.`name`",
            query.trim { it <= ' ' })

        val uri = TestContentProvider.NoteModel.withOpenId(1, true)

        val contentValues = ContentValues()
        contentValues.put(note.query, "Test")
        contentValues.put(id.query, 1)
        contentValues.put(providerModel.query, 1)

        val group = ConditionGroup.clause()
        SqlUtils.addContentValues(contentValues, group)
        for (condition in group) {
            assertTrue(condition.columnName() == "`id`" || condition.columnName() == "`providerModel`" ||
                condition.columnName() == "`note`")
        }
    }

    @Test
    fun testUpdateEffect() {
        val testUpdateModel = TestUpdateModel()
        testUpdateModel.name = "Test"
        testUpdateModel.value = "oldvalue"
        testUpdateModel.save()

        assertNotNull(SQLite.select().from(TestUpdateModel::class.java)
            .where(TestUpdateModel_Table.name.`is`("Test")))

        SQLite.update(TestUpdateModel::class.java)
            .set(TestUpdateModel_Table.value.`is`("newvalue"))
            .where().count()

        val newUpdateModel = SQLite.select().from(TestUpdateModel::class.java)
            .where(TestUpdateModel_Table.name.`is`("Test"))
            .querySingle()
        assertEquals("newvalue", newUpdateModel!!.value)

    }

    protected fun assertUpdateSuffix(suffix: String, update: Update<*>) {
        assertTrue(update.query.trim { it <= ' ' }.startsWith("UPDATE OR " + suffix))
    }

}
