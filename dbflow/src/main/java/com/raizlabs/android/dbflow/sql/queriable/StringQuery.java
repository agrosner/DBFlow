package com.raizlabs.android.dbflow.sql.queriable;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<TModel> extends BaseModelQueriable<TModel> implements Query, ModelQueriable<TModel> {

    /**
     * The full SQLite query to use
     */
    private final String query;
    private String[] args;

    /**
     * Creates an instance of this class
     *
     * @param table The table to use
     * @param sql   The sql statement to query the DB with. Does not work with {@link Delete},
     *              this must be done with {@link SQLiteDatabase#execSQL(String)}
     */
    public StringQuery(@NonNull Class<TModel> table, @NonNull String sql) {
        super(table);
        query = sql;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public FlowCursor query() {
        return query(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public FlowCursor query(@NonNull DatabaseWrapper databaseWrapper) {
        return databaseWrapper.rawQuery(query, args);
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    @NonNull
    public StringQuery<TModel> setSelectionArgs(@NonNull String[] args) {
        this.args = args;
        return this;
    }

    @NonNull
    @Override
    public BaseModel.Action getPrimaryAction() {
        return BaseModel.Action.CHANGE; // we don't explicitly know the change, but something changed.
    }
}
