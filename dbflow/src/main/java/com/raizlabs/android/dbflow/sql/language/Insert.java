package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ValueQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.Map;

/**
 * Description: The SQLite INSERT command
 */
public class Insert<ModelClass extends Model> extends BaseQueriable<ModelClass> {


    /**
     * The columns to specify in this query (optional)
     */
    private IProperty[] columns;

    /**
     * The values to specify in this query
     */
    private Object[] values;

    /**
     * The conflict algorithm to use to resolve inserts.
     */
    private ConflictAction conflictAction = ConflictAction.NONE;

    /**
     * Constructs a new INSERT command
     *
     * @param table The table to insert into
     */
    public Insert(Class<ModelClass> table) {
        super(table);
    }

    /**
     * The optional columns to specify. If specified, the values length must correspond to these columns, and
     * each column has a 1-1 relationship to the values.
     *
     * @param columns The columns to use
     */
    public Insert<ModelClass> columns(String... columns) {
        this.columns = new IProperty[columns.length];
        ModelAdapter<ModelClass> modelClassModelAdapter = FlowManager.getModelAdapter(getTable());
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            this.columns[i] = modelClassModelAdapter.getProperty(column);
        }
        return this;
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     */
    public Insert<ModelClass> values(Object... values) {
        this.values = values;
        return this;
    }

    /**
     * Uses the {@link Condition} pairs to fill this insert query.
     *
     * @param conditions The conditions that we use to fill the columns and values of this INSERT
     */
    public Insert<ModelClass> columnValues(SQLCondition... conditions) {

        String[] columns = new String[conditions.length];
        Object[] values = new Object[conditions.length];

        for (int i = 0; i < conditions.length; i++) {
            SQLCondition condition = conditions[i];
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    /**
     * Uses the {@link Condition} pairs to fill this insert query.
     *
     * @param conditionGroup The ConditionGroup to use
     */
    public Insert<ModelClass> columnValues(ConditionGroup conditionGroup) {

        int size = conditionGroup.size();
        String[] columns = new String[size];
        Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            SQLCondition condition = conditionGroup.getConditions().get(i);
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    public Insert<ModelClass> columnValues(ContentValues contentValues) {
        java.util.Set<Map.Entry<String, Object>> entries = contentValues.valueSet();
        int count = 0;
        String[] columns = new String[contentValues.size()];
        Object[] values = new Object[contentValues.size()];
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            columns[count] = key;
            values[count] = contentValues.get(key);
            count++;
        }

        return columns(columns).values(values);
    }


    /**
     * Specifies the optional OR method to use for this insert query
     *
     * @param action The conflict action to use
     * @return
     */
    public Insert<ModelClass> or(ConflictAction action) {
        conflictAction = action;
        return this;
    }

    /**
     * Specifies OR REPLACE, which will either insert if row does not exist, or replace the value if it does.
     *
     * @return
     */
    public Insert<ModelClass> orReplace() {
        return or(ConflictAction.REPLACE);
    }

    /**
     * Specifies OR ROLLBACK, which will cancel the current transaction or ABORT the current statement.
     *
     * @return
     */
    public Insert<ModelClass> orRollback() {
        return or(ConflictAction.ROLLBACK);
    }

    /**
     * Specifies OR ABORT, which will cancel the current INSERT, but all other operations will be preserved in
     * the current transaction.
     *
     * @return
     */
    public Insert<ModelClass> orAbort() {
        return or(ConflictAction.ABORT);
    }

    /**
     * Specifies OR FAIL, which does not back out of the previous statements. Anything else in the current
     * transaction will fail.
     *
     * @return
     */
    public Insert<ModelClass> orFail() {
        return or(ConflictAction.FAIL);
    }

    /**
     * Specifies OR IGNORE, which ignores any kind of error and proceeds as normal.
     *
     * @return
     */
    public Insert<ModelClass> orIgnore() {
        return or(ConflictAction.IGNORE);
    }

    @Override
    public String getQuery() {
        ValueQueryBuilder queryBuilder = new ValueQueryBuilder("INSERT ");
        if (conflictAction != null && !conflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR ").append(conflictAction);
        }
        queryBuilder.appendSpaceSeparated("INTO")
                .appendTableName(getTable());

        if (columns != null) {
            queryBuilder.append("(")
                    .appendArray((Object[]) columns)
                    .append(")");
        }

        if (columns != null && values != null && columns.length != values.length) {
            throw new IllegalStateException("The Insert of " + FlowManager.getTableName(getTable()) + " when specifying" +
                    "columns needs to have the same amount of values and columns");
        } else if (values == null) {
            throw new IllegalStateException("The insert of " + FlowManager.getTableName(getTable()) + " should have" +
                    "at least one value specified for the insert");
        }

        queryBuilder.append(" VALUES(").appendModelArray(values).append(")");

        return queryBuilder.getQuery();
    }

}
