package com.grosner.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Constructs a condition statement for a specific {@link com.grosner.dbflow.structure.Model} class
 */
public class ConditionQueryBuilder<ModelClass extends Model> extends QueryBuilder<ConditionQueryBuilder<ModelClass>> {

    /**
     * Default empty param that will be replaced with the actual value when we call {@link #replaceEmptyParams(Object[])}
     */
    private static final String EMPTY_PARAM = "?";

    /**
     * The structure of the ModelClass this query pertains to
     */
    private ModelAdapter<ModelClass> mTableStructure;

    /**
     * The parameters to build this query with
     */
    private LinkedHashMap<String, Condition> mParams = new LinkedHashMap<String, Condition>();

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
    private String mSeparator = "AND";

    /**
     * Constructs an instance of this class
     * and {@link ModelClass}.
     *
     * @param table      The table to use
     * @param conditions The array of conditions to add to the mapping.
     */
    public ConditionQueryBuilder(Class<ModelClass> table, Condition... conditions) {
        mTableStructure = FlowManager.getModelAdapter(table);
        putConditions(conditions);
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
                mParams.put(condition.columnName(), condition);
            }
            isChanged = true;
        }
        return this;
    }

    @Override
    public String getQuery() {
        // Empty query, we will build it now with params, or if the query has changed.
        if (isChanged || mQuery.length() == 0) {
            isChanged = false;
            mQuery = new StringBuilder();

            Set<String> keys = mParams.keySet();
            int count = 0;
            for (String key : keys) {
                appendConditionToQuery(mParams.get(key));
                if (count < keys.size() - 1) {
                    appendSpaceSeparated(mSeparator);
                }
                count++;
            }
        }

        return mQuery.toString();
    }

    /**
     * Converts the given value for the column
     *
     * @param columnName The name of the column in the DB
     * @param value      The value of the column we are looking for
     * @return
     */
    @SuppressWarnings("unchecked")
    public String convertValueToString(String columnName, Object value) {
        String stringVal;
        if (!useEmptyParams && value != null) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if(typeConverter != null) {
                value = typeConverter.getDBValue(value);
            }
        }

        if (value instanceof Number) {
            stringVal = String.valueOf(value);
        } else {
            stringVal = String.valueOf(value);
            if (!stringVal.equals(EMPTY_PARAM)) {
                stringVal = DatabaseUtils.sqlEscapeString(stringVal);
            }
        }

        return stringVal;
    }

    /**
     * Internal utility method for appending a condition to the query
     *
     * @param condition The value of the column we are looking for
     * @return
     */
    ConditionQueryBuilder<ModelClass> appendConditionToQuery(Condition condition) {
        return append(condition.columnName()).appendSpaceSeparated(condition.operation())
                .append(convertValueToString(condition.columnName(), condition.value()));
    }

    public ConditionQueryBuilder<ModelClass> setSeparator(String separator) {
        mSeparator = separator;
        return this;
    }

    public ConditionQueryBuilder<ModelClass> setUseEmptyParams(boolean useEmptyParams) {
        this.useEmptyParams = useEmptyParams;
        return this;
    }


    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     * EX: columnName = value
     *
     * @param columnName The name of the column in the DB
     * @param value      The value of the column we are looking for
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putCondition(String columnName, Object value) {
        return putCondition(columnName, "=", value);
    }

    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param columnName The name of the column in the DB
     * @param operator   The operator to use "=", "<", etc.
     * @param value      The value of the column we are looking for
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putCondition(String columnName, String operator, Object value) {
        if (useEmptyParams && !EMPTY_PARAM.equals(value)) {
            throw new IllegalStateException("The " + ConditionQueryBuilder.class.getSimpleName() + " is " +
                    "operating in empty param mode. All params must be empty");
        }
        return putCondition(Condition.column(columnName).operation(operator).value(value));

    }

    /**
     * Appends a condition to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param condition The where arguments. We can specify other operators than just "="
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putCondition(Condition condition) {
        mParams.put(condition.columnName(), condition);
        isChanged = true;
        return this;
    }

    /**
     * This will append all the primary key names with empty params. Ex: name = ?, columnName = ?
     *
     * @return
     */
    public ConditionQueryBuilder<ModelClass> emptyPrimaryConditions() {
        return append(mTableStructure.getPrimaryModelWhere());
    }

    /**
     * Appends all the parameters from the specified map
     *
     * @param params The mapping between column names and the string-represented value
     * @return
     */
    public ConditionQueryBuilder<ModelClass> putConditionMap(Map<String, Condition> params) {
        if (params != null && !params.isEmpty()) {
            mParams.putAll(params);
            isChanged = true;
        }
        return this;
    }

    /**
     * Appends an empty condition to this map that will be represented with a "?". All params must either be empty or not.
     *
     * @param columnName The name of the column in the DB
     * @return
     */
    public ConditionQueryBuilder<ModelClass> emptyCondition(String columnName) {
        useEmptyParams = true;
        return putCondition(columnName, EMPTY_PARAM);
    }

    /**
     * Returns the raw query without converting the values of {@link com.grosner.dbflow.sql.builder.Condition}.
     *
     * @return
     */
    public String getRawQuery() {
        QueryBuilder rawQuery = new QueryBuilder();

        Set<String> keys = mParams.keySet();
        int count = 0;
        for (String key : keys) {
            Condition condition = mParams.get(key);
            rawQuery.append(condition.columnName()).appendSpaceSeparated(condition.operation())
                    .append(condition.value());
            if (count < keys.size() - 1) {
                rawQuery.appendSpaceSeparated(mSeparator);
            }
            count++;
        }

        return rawQuery.toString();
    }

    /**
     * Replaces empty parameter values such as "columnName = ?" with the array of values passed in. It must
     * match the count of columns that are in this where query.
     *
     * @param values The values of the fields we wish to replace. Must match the length of the empty params and must be in empty param mode.
     * @return The query with the paramters filled in.
     */
    public ConditionQueryBuilder<ModelClass> replaceEmptyParams(Object... values) {
        if (!useEmptyParams) {
            throw new IllegalStateException("The " + ConditionQueryBuilder.class.getSimpleName() + " is " +
                    "not operating in empty param mode.");
        }
        if (mParams.size() != values.length) {
            throw new IllegalArgumentException("The count of values MUST match the number of columns they correspond to for " +
                    mTableStructure.getTableName());
        }

        ConditionQueryBuilder<ModelClass> conditionQueryBuilder =
                new ConditionQueryBuilder<ModelClass>(mTableStructure.getModelClass());
        Set<String> columnNames = mParams.keySet();

        int count = 0;
        for (String columnName : columnNames) {
            conditionQueryBuilder.putCondition(columnName, values[count]);
            count++;
        }

        return conditionQueryBuilder;
    }

    /**
     * Returns the {@link ModelClass} that this query belongs to
     *
     * @return
     */
    public Class<ModelClass> getTableClass() {
        return getModelAdapter().getModelClass();
    }

    /**
     * Returns the table structure that this {@link ConditionQueryBuilder} uses.
     *
     * @return
     */
    public ModelAdapter<ModelClass> getModelAdapter() {
        return mTableStructure;
    }

}
