package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description:
 */
public class TriggerLogic<ModelClass extends Model> implements Query {

    private TriggerMethod<ModelClass> mTriggerMethod;

    private Query mTriggerLogicQuery;

    TriggerLogic(TriggerMethod<ModelClass> triggerMethod, Query triggerLogicQuery) {
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
        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTriggerMethod.mTable);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

    /**
     * Disables this trigger
     */
    public void disable() {
        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTriggerMethod.mTable);
        QueryBuilder queryBuilder = new QueryBuilder("DROP TRIGGER IF EXISTS ")
                .append(FlowManager.getTableName(mTriggerMethod.mTable));
        databaseDefinition.getWritableDatabase().execSQL(queryBuilder.getQuery());
    }
}
