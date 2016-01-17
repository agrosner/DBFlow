package com.raizlabs.android.dbflow.test.structure;

import android.content.ContentValues;
import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.listener.ContentValuesListener;
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener;
import com.raizlabs.android.dbflow.structure.listener.SQLiteStatementListener;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class ListenerModel extends TestModel1 implements LoadFromCursorListener,
        SQLiteStatementListener, ContentValuesListener {

    private LoadFromCursorListener loadFromCursorListener;
    private SQLiteStatementListener sqLiteStatementListener;
    private ContentValuesListener contentValuesListener;

    public void registerListeners(SQLiteStatementListener sqLiteStatementListener,
                                  ContentValuesListener contentValuesListener) {

        this.sqLiteStatementListener = sqLiteStatementListener;
        this.contentValuesListener = contentValuesListener;
    }

    public void registerLoadFromCursorListener(LoadFromCursorListener loadFromCursorListener) {
        this.loadFromCursorListener = loadFromCursorListener;
    }

    @Override
    public void onBindToContentValues(ContentValues contentValues) {
        contentValuesListener.onBindToContentValues(contentValues);
    }

    @Override
    public void onBindToInsertValues(ContentValues contentValues) {
        contentValuesListener.onBindToInsertValues(contentValues);
    }

    @Override
    public void onLoadFromCursor(Cursor cursor) {
        loadFromCursorListener.onLoadFromCursor(cursor);
    }

    @Override
    public void onBindToStatement(DatabaseStatement databaseStatement) {
        sqLiteStatementListener.onBindToStatement(databaseStatement);
    }

    @Override
    public void onBindToInsertStatement(DatabaseStatement databaseStatement) {
        sqLiteStatementListener.onBindToInsertStatement(databaseStatement);
    }
}
