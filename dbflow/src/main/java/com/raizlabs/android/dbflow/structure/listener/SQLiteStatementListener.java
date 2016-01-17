package com.raizlabs.android.dbflow.structure.listener;

import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.structure.InternalAdapter;

/**
 * Description: Marks a {@link com.raizlabs.android.dbflow.structure.Model} as suscribing to
 * the {@link android.database.sqlite.SQLiteStatement} that is used to {@link Model#insert()}
 * a model into the DB.
 */
public interface SQLiteStatementListener {

    /**
     * Called at the end of {@link InternalAdapter#bindToStatement(com.raizlabs.android.dbflow.structure.database.DatabaseStatement, com.raizlabs.android.dbflow.structure.Model)}
     * Perform a custom manipulation of the statement as willed.
     *
     * @param sqLiteStatement The insert statement from the {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
     */
    void onBindToStatement(SQLiteStatement sqLiteStatement);

    void onBindToInsertStatement(SQLiteStatement sqLiteStatement);
}
