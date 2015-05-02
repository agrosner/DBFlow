package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes the method that the trigger uses.
 */
public class TriggerMethod<ModelClass extends Model> implements Query {

    public static final String DELETE = "DELETE";

    public static final String INSERT = "INSERT";

    public static final String UPDATE = "UPDATE";

    final Trigger trigger;

    private ColumnAlias[] columns;

    private final String methodName;

    /**
     * The table we're operating on.
     */
    Class<ModelClass> mOnTable;

    boolean forEachRow = false;

    private Condition mWhenCondition;

    TriggerMethod(Trigger trigger, String methodName, Class<ModelClass> onTable, String... columns) {
        this.trigger = trigger;
        this.methodName = methodName;
        mOnTable = onTable;
        if (columns != null && columns.length > 0) {
            if (!methodName.equals(UPDATE)) {
                throw new IllegalArgumentException("An Trigger OF can only be used with an UPDATE method");
            }
            this.columns = new ColumnAlias[columns.length];
            for (int i = 0; i < this.columns.length; i++) {
                this.columns[i] = ColumnAlias.column(columns[i]);
            }
        }
    }

    TriggerMethod(Trigger trigger, String methodName, Class<ModelClass> onTable, ColumnAlias... columns) {
        this.trigger = trigger;
        this.methodName = methodName;
        mOnTable = onTable;
        if (columns != null && columns.length > 0) {
            if (!methodName.equals(UPDATE)) {
                throw new IllegalArgumentException("An Trigger OF can only be used with an UPDATE method");
            }
            this.columns = columns;
        }
    }

    public TriggerMethod<ModelClass> forEachRow() {
        forEachRow = true;
        return this;
    }

    /**
     * Appends a WHEN condition after the ON tableName and before BEGIN...END
     *
     * @param condition The condition for the trigger
     * @return
     */
    public TriggerMethod<ModelClass> when(Condition condition) {
        mWhenCondition = condition;
        return this;
    }

    /**
     * Specify the logic that gets executed for this trigger. Supported statements include:
     * {@link com.raizlabs.android.dbflow.sql.language.Update}, INSERT, {@link com.raizlabs.android.dbflow.sql.language.Delete},
     * and {@link com.raizlabs.android.dbflow.sql.language.Select}
     *
     * @param triggerLogicQuery The query to run for the BEGIN..END of the trigger
     * @return This trigger
     */
    public CompletedTrigger<ModelClass> begin(Query triggerLogicQuery) {
        return new CompletedTrigger<>(this, triggerLogicQuery);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder
                = new QueryBuilder(trigger.getQuery())
                .append(methodName);
        if (columns != null && columns.length > 0) {
            queryBuilder.appendSpaceSeparated("OF")
                    .appendArray(columns);
        }
        queryBuilder.appendSpaceSeparated("ON").append(FlowManager.getTableName(mOnTable));

        if (forEachRow) {
            queryBuilder.appendSpaceSeparated("FOR EACH ROW");
        }

        if (mWhenCondition != null) {
            queryBuilder.append(" WHEN ");
            mWhenCondition.appendConditionToRawQuery(queryBuilder);
            queryBuilder.appendSpace();
        }

        queryBuilder.appendSpace();

        return queryBuilder.getQuery();
    }
}
