package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.dropTrigger
import com.raizlabs.dbflow5.sql.Query

/**
 * Description: The last piece of a TRIGGER statement, this class contains the BEGIN...END and the logic in between.
 */
class CompletedTrigger<TModel: Any> internal constructor(
    /**
     * The first pieces of this TRIGGER statement
     */
    private val triggerMethod: TriggerMethod<TModel>, triggerLogicQuery: Query) : Query {

    /**
     * The query to run between the BEGIN and END of this statement
     */
    private val triggerLogicQuery = arrayListOf<Query>()

    override val query: String
        get() =
            "${triggerMethod.query}\nBEGIN\n${triggerLogicQuery.joinToString(separator = ";\n")};\nEND"

    init {
        this.triggerLogicQuery.add(triggerLogicQuery)
    }

    /**
     * Appends the nextStatement to this query as another line to be executed by trigger.
     */
    infix fun and(nextStatement: Query) = apply {
        this.triggerLogicQuery.add(nextStatement)
    }


    /**
     * Turns on this trigger
     */
    fun enable(databaseWrapper: DatabaseWrapper) {
        databaseWrapper.execSQL(query)
    }

    /**
     * Disables this trigger
     */
    fun disable(databaseWrapper: DatabaseWrapper) {
        dropTrigger(databaseWrapper, triggerMethod.trigger.name)
    }
}
