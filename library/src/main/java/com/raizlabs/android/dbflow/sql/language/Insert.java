package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Queriable;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.ValueQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.*;

/**
 * Description: The SQLite INSERT command
 */
public class Insert<ModelClass extends Model> implements Query, Queriable {

    /**
     * The table class that this INSERT points to
     */
    private Class<ModelClass> mTable;

    /**
     * The database manager
     */
    private BaseDatabaseDefinition mManager;

    /**
     * The columns to specify in this query (optional)
     */
    private String[] mColumns;

    /**
     * The values to specify in this query
     */
    private Object[] mValues;

    /**
     * The conflict algorithm to use to resolve inserts.
     */
    private ConflictAction mConflictAction = ConflictAction.NONE;

    /**
     * Constructs a new INSERT command
     *
     * @param table The table to insert into
     */
    public Insert(Class<ModelClass> table) {
        mTable = table;
        mManager = FlowManager.getDatabaseForTable(table);
    }

    /**
     * The optional columns to specify. If specified, the values length must correspond to these columns, and
     * each column has a 1-1 relationship to the values.
     *
     * @param columns The columns to use
     * @return This INSERT statement
     */
    public Insert<ModelClass> columns(String... columns) {
        mColumns = columns;
        return this;
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     * @return
     */
    public Insert<ModelClass> values(Object... values) {
        mValues = values;
        return this;
    }

    /**
     * Uses the {@link com.raizlabs.android.dbflow.sql.builder.Condition} pairs to fill this insert query.
     *
     * @param conditions The conditions that we use to fill the columns and values of this INSERT
     * @return
     */
    public Insert<ModelClass> columnValues(Condition... conditions) {

        String[] columns = new String[conditions.length];
        Object[] values = new Object[conditions.length];

        for (int i = 0; i < conditions.length; i++) {
            Condition condition = conditions[i];
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    /**
     * Uses the {@link com.raizlabs.android.dbflow.sql.builder.Condition} pairs to fill this insert query.
     *
     * @param conditionQueryBuilder The condition query builder to use
     * @return
     */
    public Insert<ModelClass> columnValues(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {

        int size = conditionQueryBuilder.size();
        String[] columns = new String[size];
        Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            Condition condition = conditionQueryBuilder.getConditions().get(i);
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    public Insert<ModelClass> columnValues(ContentValues contentValues) {
        int count = 0;
        String[] columns = new String[contentValues.size()];
        Object[] values = new Object[contentValues.size()];
        for(Map.Entry<String, Object> entry : contentValues.valueSet()) {
            String key = entry.getKey();
            columns[count] = key;
            values[count] = contentValues.get(key);
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
        mConflictAction = action;
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

    /**
     * @return Exeuctes and returns the count of rows affected by this query.
     */
    public long count() {
        return DatabaseUtils.longForQuery(mManager.getWritableDatabase(), getQuery(), null);
    }

    @Override
    public String getQuery() {
        ValueQueryBuilder queryBuilder = new ValueQueryBuilder("INSERT ");
        if (mConflictAction != null && !mConflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR ").append(mConflictAction);
        }
        queryBuilder.appendSpaceSeparated("INTO")
                .appendTableName(mTable);

        if (mColumns != null) {
            queryBuilder.append("(")
                    .appendQuotedArray(mColumns)
                    .append(")");
        }

        if (mColumns != null && mValues != null && mColumns.length != mValues.length) {
            throw new IllegalStateException("The Insert of " + FlowManager.getTableName(mTable) + " when specifying" +
                    "columns needs to have the same amount of values and columns");
        } else if (mValues == null) {
            throw new IllegalStateException("The insert of " + FlowManager.getTableName(mTable) + " should have" +
                    "at least one value specified for the insert");
        }

        queryBuilder.append(" VALUES(").appendModelArray(mValues).append(")");

        return queryBuilder.getQuery();
    }

    /**
     * @return The table associated with this INSERT
     */
    public Class<ModelClass> getTable() {
        return mTable;
    }

    @Override
    public Cursor query() {
        FlowManager.getDatabaseForTable(mTable).getWritableDatabase().execSQL(getQuery());
        return null;
    }

    @Override
    public void queryClose() {
        query();
    }
}
