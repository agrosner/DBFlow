package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

/**
 * Description: Describes the method that the trigger uses.
 */
public class TriggerMethod<TModel> implements Query {

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
    private SQLOperator whenCondition;

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

    @NonNull
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
    @NonNull
    public TriggerMethod<TModel> when(@NonNull SQLOperator condition) {
        whenCondition = condition;
        return this;
    }

    /**
     * Specify the logic that gets executed for this trigger. Supported statements include:
     * {@link Update}, INSERT, {@link Delete},
     * and {@link Select}
     *
     * @param triggerLogicQuery The query to run for the BEGIN..END of the trigger
     * @return This trigger
     */
    @NonNull
    public CompletedTrigger<TModel> begin(@NonNull Query triggerLogicQuery) {
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
