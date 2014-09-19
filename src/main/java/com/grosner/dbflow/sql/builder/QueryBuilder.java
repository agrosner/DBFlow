package com.grosner.dbflow.sql.builder;

import android.text.TextUtils;

import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.structure.SQLiteType;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This will hold the SQLiteQuery for anything. It will be generated on the fly.
 */
public class QueryBuilder<QueryClass extends QueryBuilder> implements Query {

    /**
     * This query is backed by a {@link java.lang.StringBuilder}
     */
    protected StringBuilder mQuery = new StringBuilder();

    /**
     * Appends a string to this query
     * @param string The string to append
     * @return
     */
    public QueryClass append(String string) {
        mQuery.append(string);
        return castThis();
    }

    /**
     * Appends a space to this query
     * @return
     */
    public QueryClass appendSpace() {
        return append(" ");
    }

    /**
     * Appends the string with spaces on the front and end of the string
     * @param string The string to append
     * @return
     */
    @SuppressWarnings("unchecked")
    public QueryClass appendSpaceSeparated(String string) {
        return (QueryClass) appendSpace().append(string).appendSpace();
    }

    /**
     * Appends an {@link com.grosner.dbflow.structure.SQLiteType} to this query based on the class
     * passed in.
     * @param type The Class to look up from {@link com.grosner.dbflow.structure.SQLiteType}
     * @return
     */
    public QueryClass appendType(Class type) {
        return appendSQLiteType(SQLiteType.get(type));
    }

    /**
     * Appends the {@link com.grosner.dbflow.structure.SQLiteType} to this query
     * @param sqLiteType The {@link com.grosner.dbflow.structure.SQLiteType} to append
     * @return
     */
    public QueryClass appendSQLiteType(SQLiteType sqLiteType) {
        return append(sqLiteType.name());
    }

    /**
     * Appends an array of these objects by joining them with a comma with
     * {@link android.text.TextUtils#join(CharSequence, Object[])}
     * @param objects The array of objects to pass in
     * @return
     */
    public QueryClass appendArray(Object... objects) {
        return append(TextUtils.join(", ", objects));
    }

    /**
     * Appends a list of objects by joining them with a comma with
     * {@link android.text.TextUtils#join(CharSequence, Object[])}
     * @param objects The list of objects to pass in
     * @return
     */
    public QueryClass appendList(List<?> objects) {
        return append(TextUtils.join(", ", objects));
    }

    /**
     * Appends a value only if it's not {@link android.text.TextUtils#isEmpty(CharSequence)}
     * @param name The name of the qualifier
     * @param value The value to append after the name
     * @return
     */
    public QueryClass appendQualifier(String name, String value) {
        if (!TextUtils.isEmpty(value)) {
            append(name).appendSpaceSeparated(value);
        }
        return castThis();
    }

    /**
     * Casts the current object to the {@link QueryClass}
     * @return
     */
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
