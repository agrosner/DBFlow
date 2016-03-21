package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The last piece of a TRIGGER statement, this class contains the BEGIN...END and the logic in between.
 */
public class CompletedTrigger<ModelClass extends Model> implements Query {

    /**
     * The first pieces of this TRIGGER statement
     */
    private TriggerMethod<ModelClass> triggerMethod;

    /**
     * The query to run between the BEGIN and END of this statement
     */
    private Query triggerLogicQuery;

    CompletedTrigger(TriggerMethod<ModelClass> triggerMethod, Query triggerLogicQuery) {
        this.triggerMethod = triggerMethod;
        this.triggerLogicQuery = triggerLogicQuery;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(triggerMethod.getQuery());
        queryBuilder.append("\nBEGIN")
                .append("\n").append(triggerLogicQuery.getQuery()).append(";")
                .append("\nEND");
        return queryBuilder.getQuery();
    }


    /**
     * Turns on this trigger
     */
    public void enable() {
        DatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(triggerMethod.onTable);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

    /**
     * Disables this trigger
     */
    public void disable() {
        SqlUtils.dropTrigger(triggerMethod.onTable, triggerMethod.trigger.triggerName);
    }
}
