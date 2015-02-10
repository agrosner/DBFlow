package com.raizlabs.android.dbflow.sql;

import java.util.List;

/**
 * Description: This is used as a wrapper around {@link java.lang.StringBuilder} in order to provide more
 * database focused methods and to assist in generating queries to the DB using our SQL wrappers.
 */
public class QueryBuilder<QueryClass extends QueryBuilder> implements Query {

    /**
     * This query is backed by a {@link java.lang.StringBuilder}
     */
    protected StringBuilder mQuery = new StringBuilder();

    /**
     * Constructs this item with an empty {@link java.lang.StringBuilder}
     */
    public QueryBuilder() {
        super();
    }

    /**
     * Constructs this class with the specified String
     *
     * @param object The string to append
     */
    public QueryBuilder(Object object) {
        append(object);
    }

    /**
     * Appends a space to this query
     *
     * @return This instance
     */
    public QueryClass appendSpace() {
        return append(" ");
    }

    /**
     * Appends the string with spaces on the front and end of the string
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryClass appendSpaceSeparated(String string) {
        return (QueryClass) appendSpace().append(string).appendSpace();
    }

    /**
     * Appends the string as (string)
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryClass appendParenthesisEnclosed(Object string) {
        return (QueryClass) append("(").append(string).append(")");
    }

    /**
     * Appends an object to this query
     *
     * @param object The object to append
     * @return This instance
     */
    public QueryClass append(Object object) {
        mQuery.append(object);
        return castThis();
    }

    /**
     * Appends the object only if its not null
     *
     * @param object
     * @return This instance
     */
    public QueryClass appendOptional(Object object) {
        if (object != null) {
            append(object);
        }
        return castThis();
    }

    /**
     * Casts the current object to the {@link QueryClass}
     *
     * @return This casted instance
     */
    @SuppressWarnings("unchecked")
    protected QueryClass castThis() {
        return (QueryClass) this;
    }

    /**
     * Appends an {@link com.raizlabs.android.dbflow.sql.SQLiteType} to this query based on the class
     * passed in.
     *
     * @param type The Class to look up from {@link com.raizlabs.android.dbflow.sql.SQLiteType}
     * @return This instance
     */
    public QueryClass appendType(String type) {
        return appendSQLiteType(SQLiteType.get(type));
    }

    /**
     * Appends the {@link com.raizlabs.android.dbflow.sql.SQLiteType} to this query
     *
     * @param sqLiteType The {@link com.raizlabs.android.dbflow.sql.SQLiteType} to append
     * @return This instance
     */
    public QueryClass appendSQLiteType(SQLiteType sqLiteType) {
        return append(sqLiteType.name());
    }

    /**
     * Appends an array of these objects by joining them with a comma with
     * {@link #join(CharSequence, Object[])}
     *
     * @param objects The array of objects to pass in
     * @return This instance
     */
    public QueryClass appendArray(Object... objects) {
        return append(join(", ", objects));
    }

    /**
     * Appends a list of objects by joining them with a comma with
     * {@link #join(CharSequence, Iterable)}
     *
     * @param objects The list of objects to pass in
     * @return This instance
     */
    public QueryClass appendList(List<?> objects) {
        return append(join(", ", objects));
    }

    /**
     * Appends a value only if it's not empty or null
     *
     * @param name  The name of the qualifier
     * @param value The value to append after the name
     * @return This instance
     */
    public QueryClass appendQualifier(String name, String value) {
        if (value != null && value.length() > 0) {
            append(name).appendSpaceSeparated(value);
        }
        return castThis();
    }

    /**
     * Only appends the text if it is not null or empty
     *
     * @param string The string to append
     * @return This instance
     */
    public QueryClass appendNotEmpty(String string) {
        if (string != null && !string.isEmpty()) {
            append(string);
        }
        return castThis();
    }

    /**
     * Appends a quoted string. If the string is the all symbol '*', we do not quote it.
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryClass appendQuoted(String string) {
        if (string.equals("*"))
            return append(string);

        append("`").append(string.replace(".", "`.`")).append("`");
        return castThis();
    }

    /**
     * Appends a list of objects by quoting and joining them with a comma with
     * {@link #join(CharSequence, Iterable)}
     *
     * @param objects The list of objects to pass in
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryClass appendQuotedList(List<?> objects) {
        return appendQuoted(join("`, `", objects));
    }

    /**
     * Appends an array of these objects by quoting and joining them with a comma with
     * {@link #join(CharSequence, Object[])}
     *
     * @param objects The array of objects to pass in
     * @return This instance
     */
    public QueryClass appendQuotedArray(Object... objects) {
        return appendQuoted(join("`, `", objects));
    }

    @Override
    public String toString() {
        return getQuery();
    }

    @Override
    public String getQuery() {
        return mQuery.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }
}
