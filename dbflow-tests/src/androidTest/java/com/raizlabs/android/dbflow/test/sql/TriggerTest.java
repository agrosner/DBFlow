package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.trigger.CompletedTrigger;
import com.raizlabs.android.dbflow.sql.trigger.Trigger;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import static com.raizlabs.android.dbflow.test.sql.TestUpdateModel_Table.value;
import static com.raizlabs.android.dbflow.test.structure.TestModel1_Table.name;

public class TriggerTest extends FlowTestCase {


    public void testTriggerLanguage() {
        Where<TestModel1> logic = new Update<>(TestModel1.class)
                .set(name.is("Jason"))
                .where(name.is("Jason2"));
        String trigger = Trigger.create("MyTrigger")
                .after().insert(ConditionModel.class).begin(logic).getQuery();
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger`  AFTER INSERT ON `ConditionModel` " +
                "\nBEGIN" +
                "\n" + logic.getQuery() + ";" +
                "\nEND", trigger.trim());

        trigger = Trigger.create("MyTrigger2")
                .before().update(ConditionModel.class, name).begin(logic).getQuery();
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger2`  BEFORE UPDATE OF `name` ON `ConditionModel` " +
                "\nBEGIN" +
                "\n" + logic.getQuery() + ";" +
                "\nEND", trigger.trim());
    }

    public void testTriggerFunctions() {
        Delete.tables(TestUpdateModel.class, ConditionModel.class);

        CompletedTrigger<ConditionModel> trigger = Trigger.create("TestTrigger")
                .after().insert(ConditionModel.class).begin(new Update<>(TestUpdateModel.class)
                        .set(value.is("Fired")));

        TestUpdateModel model = new TestUpdateModel();
        model.setName("Test");
        model.value = "NotFired";
        model.save();

        trigger.enable();

        ConditionModel conditionModel = new ConditionModel();
        conditionModel.setName("Test");
        conditionModel.fraction = 0.6d;
        conditionModel.insert();

        model = new Select().from(TestUpdateModel.class)
                .where(name.is("Test")).querySingle();
        assertEquals(model.value, "Fired");

        trigger.disable();
    }

}
