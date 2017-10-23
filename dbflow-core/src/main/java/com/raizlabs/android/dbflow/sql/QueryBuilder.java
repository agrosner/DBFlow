package com.raizlabs.android.dbflow.sql;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Description: This is used as a wrapper around {@link StringBuilder} in order to provide more
 * database focused methods and to assist in generating queries to the DB using our SQL wrappers.
 */
public class QueryBuilder implements Query {

    private static final char QUOTE = '`';

    private static final Pattern QUOTE_PATTERN = Pattern.compile(QUOTE + ".*" + QUOTE);

    /**
     * This query is backed by a {@link StringBuilder}
     */
    protected StringBuilder query = new StringBuilder();

    /**
     * Constructs this item with an empty {@link StringBuilder}
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
    public QueryBuilder appendSpace() {
        return append(" ");
    }

    /**
     * Appends the string with spaces on the front and end of the string
     *
     * @param object The object to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryBuilder appendSpaceSeparated(Object object) {
        return appendSpace().append(object).appendSpace();
    }

    /**
     * Appends the string as (string)
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryBuilder appendParenthesisEnclosed(Object string) {
        return append("(").append(string).append(")");
    }

    /**
     * Appends an object to this query
     *
     * @param object The object to append
     * @return This instance
     */
    public QueryBuilder append(Object object) {
        query.append(object);
        return this;
    }

    /**
     * Appends the object only if its not null
     *
     * @param object If not null, its string representation.
     * @return This instance
     */
    public QueryBuilder appendOptional(Object object) {
        if (object != null) {
            append(object);
        }
        return this;
    }

    /**
     * Appends an {@link SQLiteType} to this query based on the class
     * passed in.
     *
     * @param type The Class to look up from {@link SQLiteType}
     * @return This instance
     */
    public QueryBuilder appendType(String type) {
        return appendSQLiteType(SQLiteType.get(type));
    }

    /**
     * Appends the {@link SQLiteType} to this query
     *
     * @param sqLiteType The {@link SQLiteType} to append
     * @return This instance
     */
    public QueryBuilder appendSQLiteType(SQLiteType sqLiteType) {
        return append(sqLiteType.name());
    }

    /**
     * Appends an array of these objects by joining them with a comma with
     * {@link #join(CharSequence, Object[])}
     *
     * @param objects The array of objects to pass in
     * @return This instance
     */
    public QueryBuilder appendArray(Object... objects) {
        return append(join(", ", objects));
    }

    /**
     * Appends a list of objects by joining them with a comma with
     * {@link #join(CharSequence, Iterable)}
     *
     * @param objects The list of objects to pass in
     * @return This instance
     */
    public QueryBuilder appendList(List<?> objects) {
        return append(join(", ", objects));
    }

    /**
     * Appends a value only if it's not empty or null
     *
     * @param name  The name of the qualifier
     * @param value The value to append after the name
     * @return This instance
     */
    public QueryBuilder appendQualifier(String name, String value) {
        if (value != null && value.length() > 0) {
            if (name != null) {
                append(name);
            }
            appendSpaceSeparated(value);
        }
        return this;
    }

    /**
     * Only appends the text if it is not null or empty
     *
     * @param string The string to append
     * @return This instance
     */
    public QueryBuilder appendNotEmpty(String string) {
        if (string != null && !string.isEmpty()) {
            append(string);
        }
        return this;
    }

    /**
     * Appends a quoted string. If the string is the all symbol '*', we do not quote it.
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryBuilder appendQuoted(String string) {
        if (string.equals("*"))
            return append(string);

        append(quote(string));
        return this;
    }

    /**
     * Appends a quoted string if needed. If the string is the all symbol '*', we do not quote it.
     *
     * @param string The string to append
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryBuilder appendQuotedIfNeeded(String string) {
        if (string.equals("*"))
            return append(string);

        append(quoteIfNeeded(string));
        return this;
    }

    /**
     * Appends a list of objects by quoting and joining them with a comma with
     * {@link #join(CharSequence, Iterable)}
     *
     * @param objects The list of objects to pass in
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public QueryBuilder appendQuotedList(List<?> objects) {
        return appendQuoted(join("`, `", objects));
    }

    /**
     * Appends an array of these objects by quoting and joining them with a comma with
     * {@link #join(CharSequence, Object[])}
     *
     * @param objects The array of objects to pass in
     * @return This instance
     */
    public QueryBuilder appendQuotedArray(Object... objects) {
        return appendQuoted(join("`, `", objects));
    }

    @Override
    public String toString() {
        return getQuery();
    }

    @Override
    public String getQuery() {
        return query.toString();
    }

    /**
     * @param columnName The column name to use.
     * @return A name in quotes. E.G. index =&gt; `index` so we can use keywords as column names without fear
     * of clashing.
     */
    public static String quote(String columnName) {
        return QUOTE + columnName.replace(".", "`.`") + QUOTE;
    }

    /**
     * Quotes the identifier if its not already quoted.
     *
     * @param name The name of the column or table.
     * @return Quoted only once.
     */
    public static String quoteIfNeeded(String name) {
        if (name != null && !isQuoted(name)) {
            return quote(name);
        } else {
            return name;
        }
    }

    /**
     * Helper method to check if name is quoted.
     *
     * @param name The name of a column or table.
     * @return true if the name is quoted. We may not want to quote something if its already so.
     */
    public static boolean isQuoted(String name) {
        return (QUOTE_PATTERN.matcher(name).find());
    }

    /**
     * Strips quotes out of a identifier if need to do so.
     *
     * @param name The name ot strip the quotes from.
     * @return A non-quoted name.
     */
    public static String stripQuotes(String name) {
        String ret = name;
        if (ret != null && isQuoted(ret)) {
            ret = ret.replace("`", "");
        }
        return ret;
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
