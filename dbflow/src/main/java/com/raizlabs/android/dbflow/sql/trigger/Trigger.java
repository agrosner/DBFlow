package com.raizlabs.android.dbflow.sql.trigger;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes an easy way to create a SQLite TRIGGER
 */
public class Trigger implements Query {

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
    final String triggerName;

    /**
     * If it's {@link #BEFORE}, {@link #AFTER}, or {@link #INSTEAD_OF}
     */
    String beforeOrAfter;

    /**
     * @param triggerName The name of the trigger to use.
     * @return A new trigger.
     */
    public static Trigger create(String triggerName) {
        return new Trigger(triggerName);
    }

    /**
     * Creates a trigger with the specified trigger name. You need to complete
     * the trigger using
     *
     * @param triggerName What we should call this trigger
     */
    private Trigger(String triggerName) {
        this.triggerName = triggerName;
    }

    /**
     * Specifies AFTER eventName
     *
     * @return
     */
    public Trigger after() {
        beforeOrAfter = AFTER;
        return this;
    }

    /**
     * Specifies BEFORE eventName
     *
     * @return
     */
    public Trigger before() {
        beforeOrAfter = BEFORE;
        return this;
    }

    /**
     * Specifies INSTEAD OF eventName
     *
     * @return
     */
    public Trigger insteadOf() {
        beforeOrAfter = INSTEAD_OF;
        return this;
    }

    /**
     * Starts a DELETE ON command
     *
     * @param onTable The table ON
     * @return
     */
    public <ModelClass extends Model> TriggerMethod<ModelClass> delete(Class<ModelClass> onTable) {
        return new TriggerMethod<>(this, TriggerMethod.DELETE, onTable);
    }

    /**
     * Starts a INSERT ON command
     *
     * @param onTable The table ON
     * @return
     */
    public <ModelClass extends Model> TriggerMethod<ModelClass> insert(Class<ModelClass> onTable) {
        return new TriggerMethod<>(this, TriggerMethod.INSERT, onTable);
    }

    /**
     * Starts an UPDATE ON command
     *
     * @param onTable    The table ON
     * @param properties if empty, will not execute an OF command. If you specify columns,
     *                   the UPDATE OF column1, column2,... will be used.
     * @return
     */
    public <ModelClass extends Model> TriggerMethod<ModelClass> update(Class<ModelClass> onTable, IProperty... properties) {
        return new TriggerMethod<>(this, TriggerMethod.UPDATE, onTable, properties);
    }

    /**
     * @return The name of this TRIGGER
     */
    public String getName() {
        return triggerName;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("CREATE TRIGGER IF NOT EXISTS ")
                .appendQuotedIfNeeded(triggerName).appendSpace()
                .appendOptional(" " + beforeOrAfter + " ");

        return queryBuilder.getQuery();
    }
}
