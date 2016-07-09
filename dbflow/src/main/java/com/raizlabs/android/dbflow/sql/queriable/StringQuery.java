package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<TModel extends Model> extends BaseModelQueriable<TModel> implements Query, ModelQueriable<TModel> {
    private static class ParamsHolder {
        int index;
        Object value;
        ParamType type;

        public ParamsHolder(int index, Object value, ParamType type) {
            this.index = index;
            this.value = value;
            this.type = type;
        }
    }

    private enum ParamType {
        BLOB, STRING, LONG, DOUBLE, NULL
    }

    /**
     * The full SQLite query to use
     */
    private final String query;
    private final List<ParamsHolder> params;

    /**
     * Creates an instance of this class
     *
     * @param table The table to use
     * @param sql   The sql statement to query the DB with. Does not work with {@link com.raizlabs.android.dbflow.sql.language.Delete},
     *              this must be done with {@link android.database.sqlite.SQLiteDatabase#execSQL(String)}
     */
    public StringQuery(Class<TModel> table, String sql) {
        super(table);
        query = sql;
        params = new ArrayList<>();
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Cursor query() {
        return query(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public Cursor query(DatabaseWrapper databaseWrapper) {
        String[] selectionArgs = new String[params.size()];

        for (ParamsHolder holder : params) {
            switch (holder.type) {
                case NULL:
                    selectionArgs[holder.index] = null;
                    break;
                case DOUBLE:
                case LONG:
                case STRING:
                    selectionArgs[holder.index] = holder.value.toString();
                    break;
            }
        }

        return databaseWrapper.rawQuery(query, selectionArgs);
    }

    void bindString(int index, String name) {
        params.add(new ParamsHolder(index, name, ParamType.STRING));
    }

    void bindNull(int index) {
        params.add(new ParamsHolder(index, null, ParamType.NULL));
    }

    void bindLong(int index, long aLong) {
        params.add(new ParamsHolder(index, aLong, ParamType.LONG));
    }

    void bindDouble(int index, double aDouble) {
        params.add(new ParamsHolder(index, aDouble, ParamType.DOUBLE));
    }

    /* We probably can't support blob as of now.
    void bindBlob(int index, byte[] bytes) {
    }*/
}
