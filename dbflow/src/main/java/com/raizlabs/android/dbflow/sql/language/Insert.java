package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Description: The SQLite INSERT command
 */
public class Insert<TModel> extends BaseQueriable<TModel> implements Query {


    /**
     * The columns to specify in this query (optional)
     */
    private IProperty[] columns;

    /**
     * The values to specify in this query
     */
    private List<Collection<Object>> valuesList;

    /**
     * The conflict algorithm to use to resolve inserts.
     */
    private ConflictAction conflictAction = ConflictAction.NONE;

    private From<?> selectFrom;

    /**
     * Constructs a new INSERT command
     *
     * @param table The table to insert into
     */
    public Insert(Class<TModel> table) {
        super(table);
    }

    /**
     * The optional columns to specify. If specified, the values length must correspond to these columns, and
     * each column has a 1-1 relationship to the values.
     *
     * @param columns The columns to use
     */
    @NonNull
    public Insert<TModel> columns(String... columns) {
        this.columns = new IProperty[columns.length];
        ModelAdapter<TModel> modelClassModelAdapter = FlowManager.getModelAdapter(getTable());
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            this.columns[i] = modelClassModelAdapter.getProperty(column);
        }
        return this;
    }

    @NonNull
    public Insert<TModel> columns(IProperty... properties) {
        this.columns = new IProperty[properties.length];
        for (int i = 0; i < properties.length; i++) {
            columns[i] = properties[i];
        }
        return this;
    }

    @NonNull
    public Insert<TModel> columns(@NonNull List<IProperty> properties) {
        return columns(properties.toArray(new IProperty[properties.size()]));
    }

    /**
     * @return Appends a list of columns to this INSERT statement from the associated {@link TModel}.
     */
    @NonNull
    public Insert<TModel> asColumns() {
        columns(FlowManager.getModelAdapter(getTable()).getAllColumnProperties());
        return this;
    }

    /**
     * @return Appends a list of columns to this INSERT and ? as the values.
     */
    @NonNull
    public Insert<TModel> asColumnValues() {
        asColumns();
        if (columns != null) {
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < columns.length; i++) {
                values.add("?");
            }
            valuesList.add(values);
        }
        return this;
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     */
    @NonNull
    public Insert<TModel> values(Object... values) {
        if (this.valuesList == null) {
            this.valuesList = new ArrayList<>();
        }
        this.valuesList.add(Arrays.asList(values));
        return this;
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     */
    @NonNull
    public Insert<TModel> values(Collection<Object> values) {
        if (this.valuesList == null) {
            this.valuesList = new ArrayList<>();
        }
        this.valuesList.add(values);
        return this;
    }

    /**
     * Uses the {@link Operator} pairs to fill this insert query.
     *
     * @param conditions The conditions that we use to fill the columns and values of this INSERT
     */
    @NonNull
    public Insert<TModel> columnValues(SQLOperator... conditions) {

        String[] columns = new String[conditions.length];
        Object[] values = new Object[conditions.length];

        for (int i = 0; i < conditions.length; i++) {
            SQLOperator condition = conditions[i];
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    /**
     * Uses the {@link Operator} pairs to fill this insert query.
     *
     * @param operatorGroup The OperatorGroup to use
     */
    @NonNull
    public Insert<TModel> columnValues(OperatorGroup operatorGroup) {

        int size = operatorGroup.size();
        String[] columns = new String[size];
        Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            SQLOperator condition = operatorGroup.getConditions().get(i);
            columns[i] = condition.columnName();
            values[i] = condition.value();
        }

        return columns(columns).values(values);
    }

    @NonNull
    public Insert<TModel> columnValues(ContentValues contentValues) {
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
     * Appends the specified {@link From}, which comes from a {@link Select} statement.
     *
     * @param selectFrom The from that is continuation of {@link Select}.
     */
    @NonNull
    public Insert<TModel> select(From<?> selectFrom) {
        this.selectFrom = selectFrom;
        return this;
    }


    /**
     * Specifies the optional OR method to use for this insert query
     *
     * @param action The conflict action to use
     * @return
     */
    @NonNull
    public Insert<TModel> or(ConflictAction action) {
        conflictAction = action;
        return this;
    }

    /**
     * Specifies OR REPLACE, which will either insert if row does not exist, or replace the value if it does.
     *
     * @return
     */
    @NonNull
    public Insert<TModel> orReplace() {
        return or(ConflictAction.REPLACE);
    }

    /**
     * Specifies OR ROLLBACK, which will cancel the current transaction or ABORT the current statement.
     *
     * @return
     */
    @NonNull
    public Insert<TModel> orRollback() {
        return or(ConflictAction.ROLLBACK);
    }

    /**
     * Specifies OR ABORT, which will cancel the current INSERT, but all other operations will be preserved in
     * the current transaction.
     *
     * @return
     */
    @NonNull
    public Insert<TModel> orAbort() {
        return or(ConflictAction.ABORT);
    }

    /**
     * Specifies OR FAIL, which does not back out of the previous statements. Anything else in the current
     * transaction will fail.
     *
     * @return
     */
    @NonNull
    public Insert<TModel> orFail() {
        return or(ConflictAction.FAIL);
    }

    /**
     * Specifies OR IGNORE, which ignores any kind of error and proceeds as normal.
     *
     * @return
     */
    @NonNull
    public Insert<TModel> orIgnore() {
        return or(ConflictAction.IGNORE);
    }

    @Override
    public long executeUpdateDelete(DatabaseWrapper databaseWrapper) {
        throw new IllegalStateException("Cannot call executeUpdateDelete() from an Insert");
    }

    @Override
    public long executeUpdateDelete() {
        throw new IllegalStateException("Cannot call executeUpdateDelete() from an Insert");
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("INSERT ");
        if (conflictAction != null && !conflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR").appendSpaceSeparated(conflictAction);
        }
        queryBuilder.append("INTO")
            .appendSpace()
            .append(FlowManager.getTableName(getTable()));

        if (columns != null) {
            queryBuilder.append("(")
                .appendArray((Object[]) columns)
                .append(")");
        }

        // append FROM, which overrides values
        if (selectFrom != null) {
            queryBuilder.appendSpace().append(selectFrom.getQuery());
        } else {
            if (valuesList == null || valuesList.size() < 1) {
                throw new IllegalStateException("The insert of " + FlowManager.getTableName(getTable()) + " should have" +
                    "at least one value specified for the insert");
            } else if (columns != null) {
                for (Collection<Object> values : valuesList) {
                    if (values.size() != columns.length) {
                        throw new IllegalStateException("The Insert of " + FlowManager.getTableName(getTable()) + " when specifying" +
                            "columns needs to have the same amount of values and columns");
                    }
                }
            }

            queryBuilder.append(" VALUES(");
            for (int i = 0; i < valuesList.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(",(");
                }
                queryBuilder.append(BaseOperator.joinArguments(", ", valuesList.get(i))).append(")");
            }
        }

        return queryBuilder.getQuery();
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return BaseModel.Action.INSERT;
    }
}
