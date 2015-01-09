package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The last piece of a TRIGGER statement, this class contains the BEGIN...END and the logic in between.
 */
public class CompletedTrigger<ModelClass extends Model> implements Query {

    private TriggerMethod<ModelClass> mTriggerMethod;

    private Query mTriggerLogicQuery;

    CompletedTrigger(TriggerMethod<ModelClass> triggerMethod, Query triggerLogicQuery) {
        mTriggerMethod = triggerMethod;
        mTriggerLogicQuery = triggerLogicQuery;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(mTriggerMethod.getQuery());
        queryBuilder.append("\nBEGIN")
                .append("\n").append(mTriggerLogicQuery.getQuery()).append(";")
                .append("\nEND");
        return queryBuilder.getQuery();
    }


    /**
     * Turns on this trigger
     */
    public void enable() {
        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTriggerMethod.mOnTable);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

    /**
     * Disables this trigger
     */
    public void disable() {
        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTriggerMethod.mOnTable);
        QueryBuilder queryBuilder = new QueryBuilder("DROP TRIGGER IF EXISTS ")
                .append(mTriggerMethod.mTrigger.mTriggerName);
        databaseDefinition.getWritableDatabase().execSQL(queryBuilder.getQuery());
    }
}
