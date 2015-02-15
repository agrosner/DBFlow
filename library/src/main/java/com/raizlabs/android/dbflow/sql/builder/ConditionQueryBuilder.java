package com.raizlabs.android.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: Constructs a condition statement for a specific {@link com.raizlabs.android.dbflow.structure.Model} class.
 * This enables easy combining of conditions for SQL statements and will handle converting the model value for each column into
 * the correct database-valued-string.
 */
public class ConditionQueryBuilder<ModelClass extends Model> extends QueryBuilder<ConditionQueryBuilder<ModelClass>> {

    /**
     * Returns a string containing the tokens converted into DBValues joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    public static String joinArguments(ConditionQueryBuilder conditionQueryBuilder, CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(conditionQueryBuilder.convertValueToString(token));
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens converted into DBValues joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    public static String joinArguments(ConditionQueryBuilder conditionQueryBuilder, CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(conditionQueryBuilder.convertValueToString(token));
        }
        return sb.toString();
    }

    /**
     * The structure of the ModelClass this query pertains to
     */
    private ModelAdapter<ModelClass> mModelAdapter;

    /**
     * The parameters to build this query with
     */
    private List<Condition> mParams = new ArrayList<>();

    /**
     * Whether there is a new param, we will rebuild the query.
     */
    private boolean isChanged = false;

    /**
     * if true, all params must be empty. We will throw an exception if this is not the case.
     */
    private boolean useEmptyParams = false;

    /**
     * The separator between the conditions
     */
    private String mSeparator = Condition.Operation.AND;

    /**
     * Constructs an instance of this class
     * and {@link ModelClass}.
     *
     * @param table      The table to use
     * @param conditions The array of conditions to add to the mapping.
     */
    public ConditionQueryBuilder(Class<ModelClass> table, Condition... conditions) {
        mModelAdapter = FlowManager.getModelAdapter(table);
        putConditions(conditions);
    }

    /**
     * Appends an array of these objects by joining them with a comma by {@link #joinArguments(ConditionQueryBuilder, CharSequence, Object[])}
     * and converting each value into their proper DB value.
     *
     * @param arguments The array of arguments to pass in
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> appendArgumentArray(Object... arguments) {
        return append(joinArguments(this, ",", arguments));
    }

    /**
     * Appends a list of these objects by joining them with a comma by {@link #joinArguments(ConditionQueryBuilder, CharSequence, Iterable)}
     * and converting each value into their proper DB value.
     *
     * @param arguments The list of arguments to pass in
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> appendArgumentList(List<?> arguments) {
        return append(joinArguments(this, ",", arguments));
    }

    /**
     * Replaces a query string with the specified params as part of this query. Note: appending
     * any extra condition will invalidate this statement.
     *
     * @param selection     The string query to select with ? bindings
     * @param selectionArgs The arguments that correspond to it. Will be type-converted into proper string values.
     * @return This builder.
     */
    public ConditionQueryBuilder<ModelClass> append(String selection, Object... selectionArgs) {
        String toAppend = selection;

        for (Object o : selectionArgs) {
            toAppend = toAppend.replaceFirst("\\?", convertValueToString(o));
        }

        return super.append(toAppend);
    }

    /**
     * Clears all conditions
     */
    public void clear() {
        mParams.clear();
    }

    /**
     * @param columnName The name of the column in the DB
     * @return the specified conditions matching the specified column name.
     * Case sensitive so use the $Table class fields.
     */
    public List<Condition> getConditionsMatchingColumName(String columnName) {
        List<Condition> matching = new ArrayList<>();
        for (Condition condition : mParams) {
            if (condition.columnName().equals(columnName)) {
                matching.add(condition);
            }
        }
        return matching;
    }

    /**
     * @param value The value of the conditions we're looking for
     * @return The specified conditions containing the value we're looking for. This should be the non-type-converted object.
     */
    public List<Condition> getConditionsMatchingValue(Object value) {
        List<Condition> matching = new ArrayList<>();
        for (Condition condition : mParams) {
            if (condition.value() == null ? value == null : condition.value().equals(value)) {
                matching.add(condition);
            }
        }
        return matching;
    }

    /**
     * @return A list of conditions from this builder.
     */
    public List<Condition> getConditions() {
        return mParams;
    }

    @Override
    public String getQuery() {
        // Empty query, we will build it now with params, or if the query has changed.
        if (isChanged || mQuery.length() == 0) {
            isChanged = false;
            mQuery = new StringBuilder();

            int count = 0;
            int paramSize = mParams.size();
            for (int i = 0; i < paramSize; i++) {
                Condition tempCondition = mParams.get(i);
                appendConditionToQuery(tempCondition);
                if (count < paramSize - 1) {
                    if (tempCondition.hasSeparator()) {
                        appendSpaceSeparated(tempCondition.separator());
                    } else {
                        appendSpaceSeparated(mSeparator);
                    }
                }
                count++;
            }
        }

        return mQuery.toString();
    }

    /**
     * Converts the given value for the column if it has a type converter. Then it turns that result into a string.
     *
     * @param value The value of the column in Model format.
     * @return
     */
    @SuppressWarnings("unchecked")
    public String convertValueToString(Object value) {
        String stringVal;
        if (!useEmptyParams && value != null) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if (typeConverter != null) {
                value = typeConverter.getDBValue(value);
            }
        }

        if (value instanceof Number) {
            stringVal = String.valueOf(value);
        } else {
            stringVal = String.valueOf(value);
            if (!stringVal.equals(Condition.Operation.EMPTY_PARAM)) {
                stringVal = DatabaseUtils.sqlEscapeString(stringVal);
            }
        }

        return stringVal;
    }

    /**
     * @return Count of conditions
     */
    public int size() {
        return mParams.size();
    }

    /**
     * Internal utility method for appending a condition to the query
     *
     * @param condition The value of the column we are looking for
     * @return This instance
     */
    ConditionQueryBuilder<ModelClass> appendConditionToQuery(Condition condition) {
        condition.appendConditionToQuery(this);
        return this;
    }

    /**
     * Sets the condition separator for when we build the query.
     *
     * @param separator AND, OR, etc.
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> setSeparator(String separator) {
        mSeparator = separator;
        return this;
    }

    /**
     * Sets this class to use empty params ONLY. Cannot mix empty and non-empty for query building.
     *
     * @param useEmptyParams If true, only empty parameters will be accepted.
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> setUseEmptyParams(boolean useEmptyParams) {
        this.useEmptyParams = useEmptyParams;
        return this;
    }

    /**
     * Appends all the conditions from the specified array
     *
     * @param conditions The array of conditions to add to the mapping.
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> putConditions(Condition... conditions) {
        if (conditions.length > 0) {
            for (Condition condition : conditions) {
                putCondition(condition);
            }
            isChanged = true;
        }
        return this;
    }

    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     * EX: columnName = value
     *
     * @param columnName The name of the column in the DB
     * @param value      The value of the column we are looking for
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putCondition(String columnName, Object value) {
        return putCondition(columnName, Condition.Operation.EQUALS, value);
    }

    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param columnName The name of the column in the DB
     * @param operator   The operator to use "=", "<", etc.
     * @param value      The value of the column we are looking for
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putCondition(String columnName, String operator, Object value) {
        if (useEmptyParams && !Condition.Operation.EMPTY_PARAM.equals(value)) {
            throw new IllegalStateException("The " + ConditionQueryBuilder.class.getSimpleName() + " is " +
                    "operating in empty param mode. All params must be empty");
        }
        return putCondition(Condition.column(columnName).operation(operator).value(value));

    }

    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param condition The condition to append
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> putCondition(Condition condition) {
        mParams.add(condition);
        isChanged = true;
        return this;
    }

    /**
     * Appends all the parameters from the specified list
     * <p/>
     *
     * @param params The list of conditions
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> putConditions(List<Condition> params) {
        if (params != null && !params.isEmpty()) {
            mParams.addAll(params);
            isChanged = true;
        }
        return this;
    }

    /**
     * Appends all the parameters from the specified map. The keys are ignored and now just take the values.
     * This is for backportability.
     *
     * @param params
     * @return
     * @deprecated {@link #putConditions(java.util.List)}
     */
    @Deprecated
    public ConditionQueryBuilder<ModelClass> putConditionMap(Map<String, Condition> params) {
        if (params != null && !params.isEmpty()) {
            mParams.addAll(params.values());
            isChanged = true;
        }
        return this;
    }

    /**
     * Appends an empty condition to this map that will be represented with a "?". All params must either be empty or not.
     *
     * @param columnName The name of the column in the DB
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> emptyCondition(String columnName) {
        useEmptyParams = true;
        return putCondition(columnName, Condition.Operation.EMPTY_PARAM);
    }

    /**
     * Returns the raw query without converting the values of {@link com.raizlabs.android.dbflow.sql.builder.Condition}.
     *
     * @return
     */
    public String getRawQuery() {
        QueryBuilder rawQuery = new QueryBuilder();

        int count = 0;
        int paramSize = mParams.size();
        for (int i = 0; i < paramSize; i++) {
            Condition condition = mParams.get(i);
            condition.appendConditionToRawQuery(rawQuery);
            if (count < paramSize - 1) {
                if (condition.hasSeparator()) {
                    rawQuery.appendSpaceSeparated(condition.separator());
                } else {
                    rawQuery.appendSpaceSeparated(mSeparator);
                }
            }
            count++;
        }

        return rawQuery.toString();
    }

    /**
     * This will append all the primary key names with empty params from the underlying {@link com.raizlabs.android.dbflow.structure.ModelAdapter}.
     * Ex: name = ?, columnName = ?
     * <p/>
     * This method is deprecated, it is faulty because we append the {@link com.raizlabs.android.dbflow.structure.ModelAdapter} primary where
     * query for it without regard for if the query has already other data within it.
     *
     * @return This instance
     */
    @Deprecated
    public ConditionQueryBuilder<ModelClass> emptyPrimaryConditions() {
        return append(mModelAdapter.getPrimaryModelWhere());
    }

    /**
     * Replaces empty parameter values such as "columnName = ?" with the array of values passed in. It must
     * match the count of columns that are in this where query.
     *
     * @param values The values of the fields we wish to replace. Must match the length of the empty params and must be in empty param mode.
     * @return The query with the parameters filled in.
     */
    public ConditionQueryBuilder<ModelClass> replaceEmptyParams(Object... values) {
        if (!useEmptyParams) {
            throw new IllegalStateException("The " + ConditionQueryBuilder.class.getSimpleName() + " is " +
                    "not operating in empty param mode.");
        }
        if (mParams.size() != values.length) {
            throw new IllegalArgumentException("The count of values MUST match the number of columns they correspond to for " +
                    mModelAdapter.getTableName());
        }

        ConditionQueryBuilder<ModelClass> conditionQueryBuilder =
                new ConditionQueryBuilder<ModelClass>(mModelAdapter.getModelClass());
        for (int i = 0; i < values.length; i++) {
            conditionQueryBuilder.putCondition(mParams.get(i).columnName(), values[i]);
        }

        return conditionQueryBuilder;
    }

    /**
     * @return the {@link ModelClass} that this query belongs to
     */
    public Class<ModelClass> getTableClass() {
        return getModelAdapter().getModelClass();
    }

    /**
     * @return the ModelAdapter that this {@link ConditionQueryBuilder} uses.
     */
    public ModelAdapter<ModelClass> getModelAdapter() {
        return mModelAdapter;
    }

    /**
     * Sets the previous condition to use the OR separator.
     *
     * @param condition The condition to "OR"
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> or(Condition condition) {
        setPreviousSeparator(Condition.Operation.OR);
        putCondition(condition);
        return this;
    }

    /**
     * Sets the previous condition to use the AND separator
     *
     * @param condition The condition to "AND"
     * @return This instance
     */
    public ConditionQueryBuilder<ModelClass> and(Condition condition) {
        setPreviousSeparator(Condition.Operation.AND);
        putCondition(condition);
        return this;
    }

    /**
     * Sets the last condition to use the separator specified
     *
     * @param separator AND, OR, etc.
     */
    protected void setPreviousSeparator(String separator) {
        if (mParams.size() > 0) {
            // set previous to use OR separator
            mParams.get(mParams.size() - 1).separator(separator);
        }
    }

}
