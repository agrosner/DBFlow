package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.sql.language.From;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.sql.language.Update;
import com.grosner.dbflow.sql.language.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class UpdateTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "update";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(TestUpdateModel.class);
    }

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

        assertEquals("UPDATE TestModel1 SET name = 'newvalue' WHERE name = 'oldvalue'", where.getQuery().trim());
        where.query();
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

    private static class TestUpdateModel extends TestModel1 {
        @Column
        private String value;
    }
}
