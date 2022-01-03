package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.dropTrigger
import com.dbflow5.sql.Query

/**
 * Description: The last piece of a TRIGGER statement, this class contains the BEGIN...END and the logic in between.
 */
class CompletedTrigger<TModel> internal constructor(
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
    suspend fun enable(databaseWrapper: DatabaseWrapper) {
        databaseWrapper.execSQL(query)
    }

    /**
     * Disables this trigger
     */
    suspend fun disable(databaseWrapper: DatabaseWrapper) {
        dropTrigger(databaseWrapper, triggerMethod.trigger.name)
    }
}
