package com.raizlabs.android.dbflow.sql.trigger;

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

    final String mTriggerName;

    String mEventName;

    String mBeforeOrAfter;

    /**
     * Creates a trigger with the specified trigger name. You need to complete
     * the trigger using
     *
     * @param triggerName
     * @return
     */
    public static Trigger create(String triggerName) {
        return new Trigger(triggerName);
    }

    private Trigger(String triggerName) {
        mTriggerName = triggerName;
    }

    /**
     * Specifies AFTER eventName
     *
     * @param eventName The name to put after some event.
     * @return
     */
    public Trigger<ModelClass> after(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = AFTER;
        return this;
    }

    /**
     * Specifies BEFORE eventName
     *
     * @param eventName The name to put before some event.
     * @return
     */
    public Trigger<ModelClass> before(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = BEFORE;
        return this;
    }

    /**
     * Specifies INSTEAD OF eventName
     *
     * @param eventName The name to put instead of some event.
     * @return
     */
    public Trigger<ModelClass> insteadOf(String eventName) {
        mEventName = eventName;
        mBeforeOrAfter = INSTEAD_OF;
        return this;
    }

    /**
     * Starts a DELETE ON command
     *
     * @return
     */
    public TriggerMethod<ModelClass> delete() {
        return new TriggerMethod<>(this, TriggerMethod.DELETE);
    }

    /**
     * Starts a INSERT ON command
     *
     * @return
     */
    public TriggerMethod<ModelClass> insert() {
        return new TriggerMethod<>(this, TriggerMethod.INSERT);
    }

    /**
     * Starts an UPDATE ON command
     *
     * @param ofColumns if empty, will not execute an OF command. If you specify columns,
     *                  the UPDATE OF column1, column2,... will be used.
     * @return
     */
    public TriggerMethod<ModelClass> update(String... ofColumns) {
        return new TriggerMethod<>(this, TriggerMethod.UPDATE, ofColumns);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("CREATE TRIGGER IF NOT EXISTS")
                .appendSpaceSeparated(mTriggerName)
                .appendOptional(" " + mBeforeOrAfter + " ");

        return queryBuilder.getQuery();
    }
}
