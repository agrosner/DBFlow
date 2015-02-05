package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1$Table;

/**
 * Description:
 */
public class UpdateTest extends FlowTestCase {

    public void testUpdateStatement() {
        Update update = new Update();

        // Verify update prefix

        assertUpdateSuffix("ROLLBACK", update.orRollback());
        assertUpdateSuffix("ABORT", update.orAbort());
        assertUpdateSuffix("REPLACE", update.orReplace());
        assertUpdateSuffix("FAIL", update.orFail());
        assertUpdateSuffix("IGNORE", update.orIgnore());

        From<TestModel1> from = new Update().table(TestModel1.class);

        assertEquals("UPDATE TestModel1", from.getQuery().trim());

        Where<TestModel1> where = from.set(Condition.column("name").is("newvalue"))
                .where(Condition.column("name").is("oldvalue"));

        assertEquals("UPDATE TestModel1 SET name='newvalue' WHERE name='oldvalue'", where.getQuery().trim());
        where.query();

        String query = new Update().table(BoxedModel.class).set(Condition.columnRaw(BoxedModel$Table.RBLNUMBER).is(BoxedModel$Table.RBLNUMBER + " + 1")).getQuery();
        assertEquals("UPDATE BoxedModel SET rblNumber=rblNumber + 1", query.trim());


        query = new Update().table(BoxedModel.class).set(Condition.column(BoxedModel$Table.RBLNUMBER).concatenateToColumn(1)).getQuery();
        assertEquals("UPDATE BoxedModel SET rblNumber=rblNumber + 1", query.trim());

        query = new Update().table(BoxedModel.class).set(Condition.column(BoxedModel$Table.NAME).concatenateToColumn("Test")).getQuery();
        assertEquals("UPDATE BoxedModel SET name=name || 'Test'", query.trim());
    }

    public void testUpdateEffect() {
        TestUpdateModel testUpdateModel = new TestUpdateModel();
        testUpdateModel.name = "Test";
        testUpdateModel.value = "oldvalue";
        testUpdateModel.save(false);

        assertNotNull(Select.byId(TestUpdateModel.class, "Test"));

        new Update().table(TestUpdateModel.class).set(Condition.column("value").is("newvalue")).where().query();

        TestUpdateModel newUpdateModel = Select.byId(TestUpdateModel.class, "Test");
        assertEquals("newvalue", newUpdateModel.value);

    }

    protected void assertUpdateSuffix(String suffix, Update update) {
        assertEquals("UPDATE OR " + suffix, update.getQuery().trim());
    }

}
