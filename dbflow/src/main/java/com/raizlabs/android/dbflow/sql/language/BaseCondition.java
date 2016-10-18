package com.raizlabs.android.dbflow.sql.language;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;

/**
 * Description: Base class for all kinds of {@link SQLCondition}
 */
public abstract class BaseCondition implements SQLCondition {

    public static String convertValueToString(Object value, boolean appendInnerQueryParenthesis) {
        return convertValueToString(value, appendInnerQueryParenthesis, true);
    }

    /**
     * Converts a value input into a String representation of that.
     * <p>
     * If it has a {@link TypeConverter}, it first will convert it's value into its {@link TypeConverter#getDBValue(Object)}.
     * <p>
     * If the value is a {@link Number}, we return a string rep of that.
     * <p>
     * If the value is a {@link BaseModelQueriable} and appendInnerQueryParenthesis is true,
     * we return the query wrapped in "()"
     * <p>
     * If the value is a {@link NameAlias}, we return the {@link NameAlias#getQuery()}
     * <p>
     * If the value is a {@link SQLCondition}, we {@link SQLCondition#appendConditionToQuery(QueryBuilder)}.
     * <p>
     * If the value is a {@link Query}, we simply call {@link Query#getQuery()}.
     * <p>
     * If the value if a {@link Blob} or byte[]
     *
     * @param value                       The value of the column in Model format.
     * @param appendInnerQueryParenthesis if its a {@link BaseModelQueriable} and an inner query value
     *                                    in a condition, we append parenthesis to the query.
     * @return Returns the result as a string that's safe for SQLite.
     */
    @SuppressWarnings("unchecked")
    public static String convertValueToString(Object value, boolean appendInnerQueryParenthesis,
                                              boolean typeConvert) {
        if (value == null) {
            return "NULL";
        } else {
            String stringVal;
            if (typeConvert) {
                TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
                if (typeConverter != null) {
                    value = typeConverter.getDBValue(value);
                }
            }

            if (value instanceof Number) {
                stringVal = String.valueOf(value);
            } else if (value instanceof Enum) {
                stringVal = ((Enum) value).name();
            } else {
                if (appendInnerQueryParenthesis && value instanceof BaseModelQueriable) {
                    stringVal = String.format("(%1s)", ((BaseModelQueriable) value).getQuery().trim());
                } else if (value instanceof NameAlias) {
                    stringVal = ((NameAlias) value).getQuery();
                } else if (value instanceof SQLCondition) {
                    QueryBuilder queryBuilder = new QueryBuilder();
                    ((SQLCondition) value).appendConditionToQuery(queryBuilder);
                    stringVal = queryBuilder.toString();
                } else if (value instanceof Query) {
                    stringVal = ((Query) value).getQuery();
                } else if (value instanceof Blob || value instanceof byte[]) {
                    byte[] bytes;
                    if (value instanceof Blob) {
                        bytes = ((Blob) value).getBlob();
                    } else {
                        bytes = ((byte[]) value);
                    }
                    stringVal = "X" + DatabaseUtils.sqlEscapeString(SqlUtils.byteArrayToHexString(bytes));
                } else {
                    stringVal = String.valueOf(value);
                    if (!stringVal.equals(Condition.Operation.EMPTY_PARAM)) {
                        stringVal = DatabaseUtils.sqlEscapeString(stringVal);
                    }
                }
            }

            return stringVal;
        }
    }

    /**
     * Returns a string containing the tokens joined by delimiters and converted into the property
     * values for a query.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling {@link #convertValueToString(Object, boolean)}.
     * @return A joined string
     */
    public static String joinArguments(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(convertValueToString(token, false));
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters and converted into the property
     * values for a query.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an {@link Iterable} of objects to be joined. Strings will be formed from
     *                  the objects by calling {@link #convertValueToString(Object, boolean)}.
     * @return A joined string
     */
    public static String joinArguments(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(convertValueToString(token, false));
        }
        return sb.toString();
    }

    /**
     * The operation such as "=", "&lt;", and more
     */
    protected String operation = "";

    /**
     * The value of the column we care about
     */
    protected Object value;

    /**
     * The column name
     */
    protected NameAlias nameAlias;

    /**
     * A custom SQL statement after the value of the Condition
     */
    protected String postArg;

    /**
     * An optional separator to use when chaining these together
     */
    protected String separator;

    /**
     * If true, the value is set and we should append it. (to prevent false positive nulls)
     */
    protected boolean isValueSet;

    BaseCondition(NameAlias nameAlias) {
        this.nameAlias = nameAlias;
    }

    /**
     * @return the value of the argument
     */
    @Override
    public Object value() {
        return value;
    }

    /**
     * @return the column name
     */
    @Override
    public String columnName() {
        return nameAlias.getQuery();
    }

    @Override
    public SQLCondition separator(String separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public String separator() {
        return separator;
    }

    /**
     * @return true if has a separator defined for this condition.
     */
    @Override
    public boolean hasSeparator() {
        return separator != null && (separator.length() > 0);
    }

    /**
     * @return the operator such as "&lt;", "&gt;", or "="
     */
    public String operation() {
        return operation;
    }

    /**
     * @return An optional post argument for this condition
     */
    public String postArgument() {
        return postArg;
    }

    /**
     * @return internal alias used for subclasses.
     */
    NameAlias columnAlias() {
        return nameAlias;
    }

    public String convertObjectToString(Object object, boolean appendInnerParenthesis) {
        return convertValueToString(object, appendInnerParenthesis);
    }

}
