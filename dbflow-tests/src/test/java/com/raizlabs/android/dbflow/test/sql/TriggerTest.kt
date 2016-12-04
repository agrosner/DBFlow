package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.language.Update
import com.raizlabs.android.dbflow.sql.language.Where
import com.raizlabs.android.dbflow.sql.trigger.CompletedTrigger
import com.raizlabs.android.dbflow.sql.trigger.Trigger
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1

import org.junit.Test

import com.raizlabs.android.dbflow.test.sql.TestUpdateModel_Table.value
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table.name
import org.junit.Assert.assertEquals

class TriggerTest : FlowTestCase() {

    @Test
    fun testTriggerLanguage() {
        val logic = Update(TestModel1::class.java)
                .set(name.`is`("Jason"))
                .where(name.`is`("Jason2"))
        var trigger = Trigger.create("MyTrigger")
                .after().insert(ConditionModel::class.java).begin(logic).query
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger`  AFTER INSERT ON `ConditionModel` " +
                "\nBEGIN" +
                "\n" + logic.query + ";" +
                "\nEND", trigger.trim { it <= ' ' })

        trigger = Trigger.create("MyTrigger2")
                .before().update(ConditionModel::class.java, name).begin(logic).query
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger2`  BEFORE UPDATE OF `name` ON `ConditionModel` " +
                "\nBEGIN" +
                "\n" + logic.query + ";" +
                "\nEND", trigger.trim { it <= ' ' })
    }

    @Test
    fun testTriggerFunctions() {
        Delete.tables(TestUpdateModel::class.java, ConditionModel::class.java)

        val trigger = Trigger.create("TestTrigger")
                .after().insert(ConditionModel::class.java).begin(Update(TestUpdateModel::class.java)
                .set(value.`is`("Fired")))

        var model = TestUpdateModel()
        model.name = "Test"
        model.value = "NotFired"
        model.save()

        trigger.enable()

        val conditionModel = ConditionModel()
        conditionModel.name = "Test"
        conditionModel.fraction = 0.6
        conditionModel.insert()

        model = Select().from(TestUpdateModel::class.java)
                .where(name.`is`("Test")).querySingle()
        assertEquals(model.value, "Fired")

        trigger.disable()
    }

}
