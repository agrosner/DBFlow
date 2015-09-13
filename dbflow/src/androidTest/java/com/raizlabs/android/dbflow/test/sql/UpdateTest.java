package com.raizlabs.android.dbflow.test.sql;

import android.content.ContentValues;
import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.provider.TestContentProvider;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import static com.raizlabs.android.dbflow.test.provider.NoteModel_Table.isOpen;
import static com.raizlabs.android.dbflow.test.provider.NoteModel_Table.note;
import static com.raizlabs.android.dbflow.test.provider.NoteModel_Table.providerModel;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.id;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.integerField;
import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.name;
import static com.raizlabs.android.dbflow.test.sql.TestUpdateModel_Table.value;

public class UpdateTest extends FlowTestCase {

    public void testUpdateStatement() {
        Update update = new Update<>(TestModel1.class);

        // Verify update prefix

        assertUpdateSuffix("ROLLBACK", update.orRollback());
        assertUpdateSuffix("ABORT", update.orAbort());
        assertUpdateSuffix("REPLACE", update.orReplace());
        assertUpdateSuffix("FAIL", update.orFail());
        assertUpdateSuffix("IGNORE", update.orIgnore());

        Update<TestModel1> from = new Update<>(TestModel1.class);

        assertEquals("UPDATE `TestModel1`", from.getQuery().trim());

        Where<TestModel1> where = from.set(Condition.column("name").is("newvalue"))
                .where(Condition.column("name").is("oldvalue"));

        assertEquals("UPDATE `TestModel1` SET `name`='newvalue' WHERE `name`='oldvalue'", where.getQuery().trim());
        where.query();

        String query = new Update<>(BoxedModel.class).set(integerField.eq(integerField + " + 1")).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `integerField`=integerField + 1", query.trim());


        query = new Update<>(BoxedModel.class).set(integerField.concatenateToColumn(1)).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `integerField`=`integerField` + 1", query.trim());

        query = new Update<>(BoxedModel.class).set(name.concatenateToColumn("Test")).getQuery();
        assertEquals("UPDATE `BoxedModel` SET `name`=`name` || 'Test'", query.trim());

        query = new Update<>(BoxedModel.class).set(name.eq("Test"))
                .where(name.eq(name.withTable()))
                .getQuery();
        assertEquals("UPDATE `BoxedModel` SET `name`='Test' WHERE `name`=`BoxedModel`.`name`", query.trim());

        Uri uri = TestContentProvider.NoteModel.withOpenId(1, true);

        ContentValues contentValues = new ContentValues();
        contentValues.put(note.getQuery(), "Test");
        contentValues.put(id.getQuery(), 1);
        contentValues.put(providerModel.getQuery(), 1);

        query = new Update<>(FlowManager.getTableClassForName("content", "NoteModel"))
                .conflictAction(ConflictAction.ABORT)
                .set().conditionValues(contentValues)
                .where(name.eq("test"))
                .and(id.is(Long.valueOf(uri.getPathSegments().get(1))))
                .and(isOpen.is(Boolean.valueOf(uri.getPathSegments().get(2))))
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
        testUpdateModel.save();

        assertNotNull(new Select().from(TestUpdateModel.class)
                .where(TestUpdateModel_Table.name.is("Test")));

        new Update<>(TestUpdateModel.class).set(TestUpdateModel_Table.value.is("newvalue")).where().count();

        TestUpdateModel newUpdateModel = new Select().from(TestUpdateModel.class)
                .where(TestUpdateModel_Table.name.is("Test"))
                .querySingle();
        assertEquals("newvalue", newUpdateModel.value);

    }

    protected void assertUpdateSuffix(String suffix, Update update) {
        assertTrue(update.getQuery().trim().startsWith("UPDATE OR " + suffix));
    }

}
