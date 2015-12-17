package com.raizlabs.android.dbflow.test.structure;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.listener.ContentValuesListener;
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener;
import com.raizlabs.android.dbflow.structure.listener.SQLiteStatementListener;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.test.structure.ListenerModel_Table.name;

public class ListenerModelTest extends FlowTestCase {

    public void testListeners() {
        Delete.table(ListenerModel.class);

        ListenerModel listenerModel = new ListenerModel();
        listenerModel.name = "This is a test";
        final boolean[] called = new boolean[]{false, false, false};
        listenerModel.registerListeners(
                new SQLiteStatementListener() {
                    @Override
                    public void onBindToStatement(SQLiteStatement sqLiteStatement) {
                        called[1] = true;
                    }

                    @Override
                    public void onBindToInsertStatement(SQLiteStatement sqLiteStatement) {
                        called[1] = true;
                    }
                },
                new ContentValuesListener() {
                    @Override
                    public void onBindToContentValues(ContentValues contentValues) {
                        called[2] = true;
                    }

                    @Override
                    public void onBindToInsertValues(ContentValues contentValues) {
                        called[2] = true;
                    }
                });
        listenerModel.registerLoadFromCursorListener(new LoadFromCursorListener() {
            @Override
            public void onLoadFromCursor(Cursor cursor) {
                called[0] = true;
            }
        });
        listenerModel.insert();
        listenerModel.update();

        ModelAdapter<ListenerModel> modelModelAdapter = FlowManager.getModelAdapter(ListenerModel.class);
        Cursor cursor = new Select().from(ListenerModel.class).where(name.is("This is a test")).query();
        assertNotNull(cursor);

        assertTrue(cursor.moveToFirst());
        modelModelAdapter.loadFromCursor(cursor, listenerModel);

        listenerModel.delete();
        cursor.close();

        for (boolean call : called) {
            assertTrue(call);
        }
    }
}
