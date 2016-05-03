package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.database.sqlite.SQLiteDoneException;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Base implementation of something that can be queried from the database.
 */
public abstract class BaseQueriable<TModel extends Model> implements Queriable {


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
     * @return Exeuctes and returns the count of rows affected by this query.
     */
    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        try {
            return SqlUtils.longForQuery(databaseWrapper, getQuery());
        } catch (SQLiteDoneException sde) {
            // catch exception here, log it but return 0;
            FlowLog.log(FlowLog.Level.E, sde);
        }
        return 0;
    }

    @Override
    public long count() {
        return count(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
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
