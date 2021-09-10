package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: The last piece of a TRIGGER statement, this class contains the BEGIN...END and the logic in between.
 */
public class CompletedTrigger<TModel> implements Query {

    /**
     * The first pieces of this TRIGGER statement
     */
    private TriggerMethod<TModel> triggerMethod;

    /**
     * The query to run between the BEGIN and END of this statement
     */
    private final List<Query> triggerLogicQuery = new ArrayList<>();

    CompletedTrigger(TriggerMethod<TModel> triggerMethod, Query triggerLogicQuery) {
        this.triggerMethod = triggerMethod;
        this.triggerLogicQuery.add(triggerLogicQuery);
    }

    /**
     * Appends the nextStatement to this query as another line to be executed by trigger.
     */
    @NonNull
    public CompletedTrigger<TModel> and(@NonNull Query nextStatement) {
        this.triggerLogicQuery.add(nextStatement);
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(triggerMethod.getQuery());
        queryBuilder.append("\nBEGIN")
            .append("\n").append(QueryBuilder.join(";\n", triggerLogicQuery)).append(";")
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
