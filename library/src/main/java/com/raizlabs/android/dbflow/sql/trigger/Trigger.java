package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes an easy way to create a SQLite TRIGGER
 */
public class Trigger<ModelClass extends Model> implements Query {

    /**
     * Specifies that we should do this TRIGGER before some event
     */
    public static final String BEFORE = "BEFORE";

    /**
     * Specifies that we should do this TRIGGER after some event
     */
    public static final String AFTER = "AFTER";

    /**
     * Specifies that we should do this TRIGGER instead of the specified events
     */
    public static final String INSTEAD_OF = "INSTEAD OF";

    /**
     * The name in the DB
     */
    final String mTriggerName;

    /**
     * If it's {@link #BEFORE}, {@link #AFTER}, or {@link #INSTEAD_OF}
     */
    String mBeforeOrAfter;

    /**
     * Creates a trigger with the specified trigger name. You need to complete
     * the trigger using
     *
     * @param triggerName What we should call this trigger
     */
    public Trigger(String triggerName) {
        mTriggerName = triggerName;
    }

    /**
     * Specifies AFTER eventName
     *
     * @return
     */
    public Trigger<ModelClass> after() {
        mBeforeOrAfter = AFTER;
        return this;
    }

    /**
     * Specifies BEFORE eventName
     *
     * @return
     */
    public Trigger<ModelClass> before() {
        mBeforeOrAfter = BEFORE;
        return this;
    }

    /**
     * Specifies INSTEAD OF eventName
     *
     * @return
     */
    public Trigger<ModelClass> insteadOf() {
        mBeforeOrAfter = INSTEAD_OF;
        return this;
    }

    /**
     * Starts a DELETE ON command
     *
     * @param onTable The table ON
     * @return
     */
    public TriggerMethod<ModelClass> delete(Class<ModelClass> onTable) {
        return new TriggerMethod<>(this, TriggerMethod.DELETE, onTable);
    }

    /**
     * Starts a INSERT ON command
     *
     * @param onTable The table ON
     * @return
     */
    public TriggerMethod<ModelClass> insert(Class<ModelClass> onTable) {
        return new TriggerMethod<>(this, TriggerMethod.INSERT, onTable);
    }

    /**
     * Starts an UPDATE ON command
     *
     * @param onTable   The table ON
     * @param ofColumns if empty, will not execute an OF command. If you specify columns,
     *                  the UPDATE OF column1, column2,... will be used.
     * @return
     */
    public TriggerMethod<ModelClass> update(Class<ModelClass> onTable, String... ofColumns) {
        return new TriggerMethod<>(this, TriggerMethod.UPDATE, onTable, ofColumns);
    }

    /**
     * @return The name of this TRIGGER
     */
    public String getName() {
        return mTriggerName;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("CREATE TRIGGER IF NOT EXISTS")
                .appendSpaceSeparated(mTriggerName)
                .appendOptional(" " + mBeforeOrAfter + " ");

        return queryBuilder.getQuery();
    }
}
