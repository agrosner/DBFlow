package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes an easy way to create a SQLite TRIGGER
 */
public class Trigger<ModelClass extends Model> implements Query {

    public static final String BEFORE = "BEFORE";

    public static final String AFTER = "AFTER";

    public static final String INSTEAD_OF = "INSTEAD OF";

    private final String mTriggerName;

    private String mEventName;

    private String mBeforeOrAfter;

    private Class<ModelClass> mTable;

    private Query mTriggerLogic;

    public static Trigger create(String triggerName) {
        return new Trigger(triggerName);
    }

    private Trigger(String triggerName) {
        mTriggerName = triggerName;
    }

    public Trigger<ModelClass> after(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = AFTER;
        return this;
    }

    public Trigger<ModelClass> before(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = BEFORE;
        return this;
    }

    public Trigger<ModelClass> insteadOf(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = eventName;
    }

    public Trigger<ModelClass> on(Class<ModelClass> modelClass) {
        mTable = modelClass;
        return this;
    }

    public Trigger logic(Query logicQuery) {
        mTriggerLogic = logicQuery;
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("CREATE TRIGGER IF NOT EXISTS")
                .appendSpaceSeparated(mTriggerName)
                .appendOptional(" " + mBeforeOrAfter + " ")
                .append(mEventName)
                .appendSpaceSeparated("ON").append(FlowManager.getTableName(mTable))
                .append("\nBEGIN")
                .append("\n").append(mTriggerLogic).append(";")
                .append("\nEND");

        return queryBuilder.getQuery();
    }

    /**
     * Turns on this trigger
     */
    public void enable() {
        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTable);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

}
