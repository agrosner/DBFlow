package com.dbflow5.query

import com.dbflow5.appendOptional
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Description: Describes an easy way to create a SQLite TRIGGER
 */
class Trigger
/**
 * Creates a trigger with the specified trigger name. You need to complete
 * the trigger using
 *
 * @param name What we should call this trigger
 */
private constructor(
        /**
         * The name in the DB
         */
        /**
         * @return The name of this TRIGGER
         */
        val name: String) : Query {

    /**
     * If it's [.BEFORE], [.AFTER], or [.INSTEAD_OF]
     */
    private var beforeOrAfter: String = ""

    private var temporary: Boolean = false

    override val query: String
        get() {
            val queryBuilder = StringBuilder("CREATE ")
            if (temporary) {
                queryBuilder.append("TEMP ")
            }
            queryBuilder.append("TRIGGER IF NOT EXISTS ")
                    .appendQuotedIfNeeded(name).append(" ")
                    .appendOptional("$beforeOrAfter ")

            return queryBuilder.toString()
        }

    /**
     * Sets the trigger as temporary.
     */
    fun temporary() = apply {
        this.temporary = true
    }

    /**
     * Specifies AFTER eventName
     */
    fun after() = apply {
        beforeOrAfter = AFTER
    }

    /**
     * Specifies BEFORE eventName
     */
    fun before() = apply {
        beforeOrAfter = BEFORE
    }

    /**
     * Specifies INSTEAD OF eventName
     */
    fun insteadOf() = apply {
        beforeOrAfter = INSTEAD_OF
    }

    /**
     * Starts a DELETE ON command
     *
     * @param onTable The table ON
     */
    infix fun <TModel> deleteOn(onTable: Class<TModel>): TriggerMethod<TModel> =
            TriggerMethod(this, TriggerMethod.DELETE, onTable)

    /**
     * Starts a INSERT ON command
     *
     * @param onTable The table ON
     */
    infix fun <TModel> insertOn(onTable: Class<TModel>): TriggerMethod<TModel> =
            TriggerMethod(this, TriggerMethod.INSERT, onTable)

    /**
     * Starts an UPDATE ON command
     *
     * @param onTable    The table ON
     * @param properties if empty, will not execute an OF command. If you specify columns,
     * the UPDATE OF column1, column2,... will be used.
     */
    fun <TModel> updateOn(onTable: Class<TModel>, vararg properties: IProperty<*>): TriggerMethod<TModel> =
            TriggerMethod(this, TriggerMethod.UPDATE, onTable, *properties)

    companion object {

        /**
         * Specifies that we should do this TRIGGER before some event
         */
        @JvmField
        val BEFORE = "BEFORE"

        /**
         * Specifies that we should do this TRIGGER after some event
         */
        @JvmField
        val AFTER = "AFTER"

        /**
         * Specifies that we should do this TRIGGER instead of the specified events
         */
        @JvmField
        val INSTEAD_OF = "INSTEAD OF"

        /**
         * @param triggerName The name of the trigger to use.
         * @return A new trigger.
         */
        @JvmStatic
        fun create(triggerName: String) = Trigger(triggerName)
    }
}

infix fun <T : Any> Trigger.deleteOn(kClass: KClass<T>) = deleteOn(kClass.java)

infix fun <T : Any> Trigger.insertOn(kClass: KClass<T>) = insertOn(kClass.java)

infix fun <T : Any> Trigger.updateOn(kClass: KClass<T>) = updateOn(kClass.java)