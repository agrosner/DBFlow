package com.raizlabs.android.dbflow.sql.builder;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.structure.SQLiteType;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This will hold the SQLiteQuery for anything. It will be generated on the fly.
 */
public class QueryBuilder<QueryClass extends QueryBuilder> implements Query {

    protected StringBuilder mQuery = new StringBuilder();

    public QueryClass append(String string) {
        mQuery.append(string);
        return castThis();
    }

    public QueryClass appendSpace(){
        return append(" ");
    }

    @SuppressWarnings("unchecked")
    public QueryClass appendSpaceSeparated(String string) {
        return (QueryClass) appendSpace().append(string).appendSpace();
    }

    public QueryClass appendType(Class type) {
        return appendSQLiteType(SQLiteType.get(type));
    }

    public QueryClass appendSQLiteType(SQLiteType sqLiteType) {
        return append(sqLiteType.name());
    }

    public QueryClass appendArray(Object[] objects) {
        return append(TextUtils.join(", ", objects));
    }

    @SuppressWarnings("unchecked")
    protected QueryClass castThis() {
        return (QueryClass) this;
    }

    @Override
    public String getQuery() {
        return mQuery.toString();
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
