package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description:
 */
public abstract class BaseQueriable<ModelClass extends Model> implements Queriable, Query {


    private final Class<ModelClass> table;

    protected BaseQueriable(Class<ModelClass> table) {
        this.table = table;
    }

    /**
     * @return The table associated with this INSERT
     */
    public Class<ModelClass> getTable() {
        return table;
    }

    /**
     * @return Exeuctes and returns the count of rows affected by this query.
     */
    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        return SqlUtils.longForQuery(databaseWrapper, getQuery());
    }

    @Override
    public long count() {
        return count(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public Cursor query() {
        query(FlowManager.getDatabaseForTable(table).getWritableDatabase());
        return null;
    }

    @Override
    public Cursor query(DatabaseWrapper databaseWrapper) {
        databaseWrapper.execSQL(getQuery());
        return null;
    }

    @Override
    public void execute() {
        Cursor cursor = query();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        Cursor cursor = query(databaseWrapper);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public DatabaseStatement compileStatement() {
        return compileStatement(FlowManager.getDatabaseForTable(table).getWritableDatabase());
    }

    @Override
    public DatabaseStatement compileStatement(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(getQuery());
    }


    @Override
    public String toString() {
        return getQuery();
    }
}
