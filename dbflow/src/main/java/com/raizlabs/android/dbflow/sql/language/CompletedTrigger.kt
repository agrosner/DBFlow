package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.SqlUtils
import java.util.*

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
    private val triggerLogicQuery = ArrayList<Query>()

    override val query: String
        get() =
            "${triggerMethod.query}\nBEGIN\n${triggerLogicQuery.joinToString(separator = ";\n")};\nEND"

    init {
        this.triggerLogicQuery.add(triggerLogicQuery)
    }

    /**
     * Appends the nextStatement to this query as another line to be executed by trigger.
     */
    fun and(nextStatement: Query) = apply {
        this.triggerLogicQuery.add(nextStatement)
    }


    /**
     * Turns on this trigger
     */
    fun enable() {
        val databaseDefinition = FlowManager.getDatabaseForTable(triggerMethod.onTable)
        databaseDefinition.writableDatabase.execSQL(query)
    }

    /**
     * Disables this trigger
     */
    fun disable() {
        SqlUtils.dropTrigger(triggerMethod.onTable, triggerMethod.trigger.name)
    }
}
