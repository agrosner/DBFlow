package com.raizlabs.android.dbflow.structure.listener;

import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;

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
     * @param databaseStatement The statement from the {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
     */
    void onBindToStatement(DatabaseStatement databaseStatement);

    /**
     * Called at the end of {@link InternalAdapter#bindToInsertStatement(DatabaseStatement, Model)} (com.raizlabs.android.dbflow.structure.database.DatabaseStatement, com.raizlabs.android.dbflow.structure.Model)}
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The insert statement from the {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
     */
    void onBindToInsertStatement(DatabaseStatement databaseStatement);
}
