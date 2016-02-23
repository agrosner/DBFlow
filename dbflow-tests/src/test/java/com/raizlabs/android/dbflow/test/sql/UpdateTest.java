package com.raizlabs.android.dbflow.test.sql;

import android.content.ContentValues;
import android.net.Uri;

import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.provider.TestContentProvider;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;

import org.junit.Test;

import static com.raizlabs.android.dbflow.test.provider.NoteModel_Table.note;
import static com.raizlabs.android.dbflow.test.provider.NoteModel_Table.providerModel;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.id;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.integerField;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.name;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UpdateTest extends FlowTestCase {

    @Test
    public void testUpdateStatement() {
        Update update = SQLite.update(TestModel1.class);

        // Verify update prefix

        assertUpdateSuffix("ROLLBACK", update.orRollback());
        assertUpdateSuffix("ABORT", update.orAbort());
        assertUpdateSuffix("REPLACE", update.orReplace());
        assertUpdateSuffix("FAIL", update.orFail());
        assertUpdateSuffix("IGNORE", update.orIgnore());

        Update<TestModel1> from = SQLite.update(TestModel1.class);

        assertEquals("UPDATE `TestModel1`", from.getQuery().trim());

        Where<TestModel1> where = from.set(TestModel1_Table.name.is("newvalue"))
                .where(TestModel1_Table.name.is("oldvalue"));

        assertEquals("UPDATE `TestModel1` SET `name`='newvalue' WHERE `name`='oldvalue'", where.getQuery().trim());
        where.query();

        String query = SQLite.update(BoxedModel.class).set(integerField.concatenate(1)).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `integerField`=`integerField` + 1", query.trim());

        query = SQLite.update(BoxedModel.class).set(name.concatenate("Test")).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `name`=`name` || 'Test'", query.trim());

        query = SQLite.update(BoxedModel.class).set(name.eq("Test"))
                .where(name.eq(name.withTable()))
                .getQuery();
        assertEquals("UPDATE `BoxedModel` SET `name`='Test' WHERE `name`=`BoxedModel`.`name`", query.trim());

        Uri uri = TestContentProvider.NoteModel.withOpenId(1, true);

        ContentValues contentValues = new ContentValues();
        contentValues.put(note.getQuery(), "Test");
        contentValues.put(id.getQuery(), 1);
        contentValues.put(providerModel.getQuery(), 1);

        ConditionGroup group = ConditionGroup.clause();
        SqlUtils.addContentValues(contentValues, group);
        for (SQLCondition condition : group) {
            assertTrue(condition.columnName().equals("`id`") || condition.columnName().equals("`providerModel`") ||
                    condition.columnName().equals("`note`"));
        }
    }

    @Test
    public void testUpdateEffect() {
        TestUpdateModel testUpdateModel = new TestUpdateModel();
        testUpdateModel.setName("Test");
        testUpdateModel.value = "oldvalue";
        testUpdateModel.save();

        assertNotNull(SQLite.select().from(TestUpdateModel.class)
                .where(TestUpdateModel_Table.name.is("Test")));

        SQLite.update(TestUpdateModel.class).set(TestUpdateModel_Table.value.is("newvalue")).where().count();

        TestUpdateModel newUpdateModel = SQLite.select().from(TestUpdateModel.class)
                .where(TestUpdateModel_Table.name.is("Test"))
                .querySingle();
        assertEquals("newvalue", newUpdateModel.value);

    }

    protected void assertUpdateSuffix(String suffix, Update update) {
        assertTrue(update.getQuery().trim().startsWith("UPDATE OR " + suffix));
    }

}
