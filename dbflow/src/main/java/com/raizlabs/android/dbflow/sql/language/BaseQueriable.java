package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatementWrapper;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Base implementation of something that can be queried from the database.
 */
public abstract class BaseQueriable<TModel> implements Queriable {


    private final Class<TModel> table;

    protected BaseQueriable(Class<TModel> table) {
        this.table = table;
    }

    /**
     * @return The table associated with this INSERT
     */
    public Class<TModel> getTable() {
        return table;
    }

    /**
     * Execute a statement that returns a 1 by 1 table with a numeric value.
     * For example, SELECT COUNT(*) FROM table.
     * Please see {@link SQLiteStatement#simpleQueryForLong()}.
     */
    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        try {
            String query = getQuery();
            FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
            return SqlUtils.longForQuery(databaseWrapper, query);
        } catch (SQLiteDoneException sde) {
            // catch exception here, log it but return 0;
            FlowLog.log(FlowLog.Level.E, sde);
        }
        return 0;
    }

    /**
     * Execute a statement that returns a 1 by 1 table with a numeric value.
     * For example, SELECT COUNT(*) FROM table.
     * Please see {@link SQLiteStatement#simpleQueryForLong()}.
     */
    @Override
    public long count() {
        return count(FlowManager.getWritableDatabaseForTable(table));
    }

    @Override
    public boolean hasData() {
        return count() > 0;
    }

    @Override
    public boolean hasData(DatabaseWrapper databaseWrapper) {
        return count(databaseWrapper) > 0;
    }

    @Override
    public Cursor query() {
        query(FlowManager.getWritableDatabaseForTable(table));
        return null;
    }

    @Override
    public Cursor query(DatabaseWrapper databaseWrapper) {
        if (getPrimaryAction().equals(BaseModel.Action.INSERT)) {
            // inserting, let's compile and insert
            DatabaseStatement databaseStatement = compileStatement(databaseWrapper);
            databaseStatement.executeInsert();
            databaseStatement.close();
        } else {
            String query = getQuery();
            FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
            databaseWrapper.execSQL(query);
        }
        return null;
    }

    @Override
    public long executeInsert() {
        return executeUpdateDelete(FlowManager.getWritableDatabaseForTable(table));
    }

    @Override
    public long executeInsert(DatabaseWrapper databaseWrapper) {
        return compileStatement().executeInsert();
    }

    @Override
    public void execute() {
        Cursor cursor = query();
        if (cursor != null) {
            cursor.close();
        } else {
            // we dont query, we're executing something here.
            SqlUtils.notifyTableChanged(getTable(), getPrimaryAction());
        }
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        Cursor cursor = query(databaseWrapper);
        if (cursor != null) {
            cursor.close();
        } else {
            // we dont query, we're executing something here.
            SqlUtils.notifyTableChanged(getTable(), getPrimaryAction());
        }
    }

    @Override
    public DatabaseStatement compileStatement() {
        return compileStatement(FlowManager.getWritableDatabaseForTable(table));
    }

    @Override
    public DatabaseStatement compileStatement(DatabaseWrapper databaseWrapper) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Compiling Query Into Statement: " + query);
        return new DatabaseStatementWrapper<>(databaseWrapper.compileStatement(query), this);
    }

    @Override
    public String toString() {
        return getQuery();
    }

    public abstract BaseModel.Action getPrimaryAction();
}
