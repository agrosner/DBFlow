package com.raizlabs.android.dbflow.test.sql;

import android.content.ContentValues;
import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.provider.NoteModel$Table;
import com.raizlabs.android.dbflow.test.provider.TestContentProvider;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

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

        assertEquals("UPDATE `TestModel1`", from.getQuery().trim());

        Where<TestModel1> where = from.set(Condition.column("name").is("newvalue"))
                .where(Condition.column("name").is("oldvalue"));

        assertEquals("UPDATE `TestModel1` SET `name`='newvalue' WHERE `name`='oldvalue'", where.getQuery().trim());
        where.query();

        String query = new Update().table(BoxedModel.class).set(Condition.columnRaw(BoxedModel$Table.INTEGERFIELD).is(
                BoxedModel$Table.INTEGERFIELD + " + 1")).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `integerField`=integerField + 1", query.trim());


        query = new Update().table(BoxedModel.class).set(
                Condition.column(BoxedModel$Table.INTEGERFIELD).concatenateToColumn(1)).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `integerField`=`integerField` + 1", query.trim());

        query = new Update().table(BoxedModel.class).set(
                Condition.column(BoxedModel$Table.NAME).concatenateToColumn("Test")).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `name`=`name` || 'Test'", query.trim());

        Uri uri = TestContentProvider.NoteModel.withOpenId(1, true);

        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteModel$Table.NOTE, "Test");
        contentValues.put(NoteModel$Table.ID, 1);
        contentValues.put(NoteModel$Table.CONTENTPROVIDERMODEL_PROVIDERMODEL, 1);

        query = new Update()
                .conflictAction(ConflictAction.ABORT)
                .table(FlowManager.getTableClassForName("content", "NoteModel"))
                .set().conditionValues(contentValues)
                .where("name = ?", "test")
                .and(Condition.column("id").is(Long.valueOf(uri.getPathSegments().get(1))))
                .and(Condition.column("isOpen").is(Boolean.valueOf(uri.getPathSegments().get(2))))
                .getQuery();

        String trimmed = query.trim().replaceFirst("UPDATE OR ABORT `NoteModel` SET ", "");
        trimmed = trimmed.replace("WHERE name = 'test' AND `id`=1 AND `isOpen`=1", "");
        trimmed = trimmed.replace("SET", "").trim();
        String[] values = trimmed.split(",");
        assertEquals(3, values.length);

        for (String value : values) {
            assertTrue(value.trim().equals("`id`=1") || value.trim().equals("`providerModel`=1") ||
                       value.trim().equals("`note`='Test'"));
        }
    }

    public void testUpdateEffect() {
        TestUpdateModel testUpdateModel = new TestUpdateModel();
        testUpdateModel.name = "Test";
        testUpdateModel.value = "oldvalue";
        testUpdateModel.save(false);

        assertNotNull(Select.byId(TestUpdateModel.class, "Test"));

        new Update().table(TestUpdateModel.class).set(Condition.column("value").is("newvalue")).where().count();

        TestUpdateModel newUpdateModel = Select.byId(TestUpdateModel.class, "Test");
        assertEquals("newvalue", newUpdateModel.value);

    }

    protected void assertUpdateSuffix(String suffix, Update update) {
        assertEquals("UPDATE OR " + suffix, update.getQuery().trim());
    }

}
