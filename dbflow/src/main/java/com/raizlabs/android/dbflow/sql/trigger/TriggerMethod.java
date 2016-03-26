package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes the method that the trigger uses.
 */
public class TriggerMethod<TModel extends Model> implements Query {

    public static final String DELETE = "DELETE";
    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";

    final Trigger trigger;
    private IProperty[] properties;
    private final String methodName;

    /**
     * The table we're operating on.
     */
    Class<TModel> onTable;
    boolean forEachRow = false;
    private SQLCondition whenCondition;

    TriggerMethod(Trigger trigger, String methodName, Class<TModel> onTable, IProperty... properties) {
        this.trigger = trigger;
        this.methodName = methodName;
        this.onTable = onTable;
        if (properties != null && properties.length > 0 && properties[0] != null) {
            if (!methodName.equals(UPDATE)) {
                throw new IllegalArgumentException("An Trigger OF can only be used with an UPDATE method");
            }
            this.properties = properties;
        }
    }

    public TriggerMethod<TModel> forEachRow() {
        forEachRow = true;
        return this;
    }

    /**
     * Appends a WHEN condition after the ON name and before BEGIN...END
     *
     * @param condition The condition for the trigger
     * @return
     */
    public TriggerMethod<TModel> when(SQLCondition condition) {
        whenCondition = condition;
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
    public CompletedTrigger<TModel> begin(Query triggerLogicQuery) {
        return new CompletedTrigger<>(this, triggerLogicQuery);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder
                = new QueryBuilder(trigger.getQuery())
                .append(methodName);
        if (properties != null && properties.length > 0) {
            queryBuilder.appendSpaceSeparated("OF")
                    .appendArray((Object[]) properties);
        }
        queryBuilder.appendSpaceSeparated("ON").append(FlowManager.getTableName(onTable));

        if (forEachRow) {
            queryBuilder.appendSpaceSeparated("FOR EACH ROW");
        }

        if (whenCondition != null) {
            queryBuilder.append(" WHEN ");
            whenCondition.appendConditionToQuery(queryBuilder);
            queryBuilder.appendSpace();
        }

        queryBuilder.appendSpace();

        return queryBuilder.getQuery();
    }
}
