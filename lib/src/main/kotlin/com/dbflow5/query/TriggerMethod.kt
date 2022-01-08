package com.dbflow5.query

import com.dbflow5.appendArray
import com.dbflow5.config.FlowManager
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Description: Describes the method that the trigger uses.
 */
class TriggerMethod<TModel : Any>
internal constructor(
    internal val trigger: Trigger, private val methodName: String,
    internal var onTable: KClass<TModel>, vararg properties: IProperty<*>
) : Query {
    private var properties: List<IProperty<*>> = mutableListOf()
    private var forEachRow = false
    private var whenCondition: SQLOperator? = null

    override val query: String
        get() {
            val queryBuilder = StringBuilder(trigger.query)
                .append(methodName)
            if (properties.isNotEmpty()) {
                queryBuilder.append(" OF ")
                    .appendArray(properties.toTypedArray())
            }
            queryBuilder.append(" ON ").append(FlowManager.getTableName(onTable))

            if (forEachRow) {
                queryBuilder.append(" FOR EACH ROW ")
            }

            whenCondition?.let { whenCondition ->
                queryBuilder.append(" WHEN ")
                whenCondition.appendConditionToQuery(queryBuilder)
                queryBuilder.append(" ")
            }

            queryBuilder.append(" ")

            return queryBuilder.toString()
        }

    init {
        if (properties.isNotEmpty() && properties.getOrNull(0) != null) {
            if (methodName != UPDATE) {
                throw IllegalArgumentException("An Trigger OF can only be used with an UPDATE method")
            }
            this.properties = properties.toList()
        }
    }

    fun forEachRow() = apply {
        forEachRow = true
    }

    /**
     * Appends a WHEN condition after the ON name and before BEGIN...END
     *
     * @param condition The condition for the trigger
     * @return
     */
    @JvmName("when")
    fun whenever(condition: SQLOperator) = apply {
        whenCondition = condition
    }

    /**
     * Specify the logic that gets executed for this trigger. Supported statements include:
     * [Update], INSERT, [Delete],
     * and [Select]
     *
     * @param triggerLogicQuery The query to run for the BEGIN..END of the trigger
     * @return This trigger
     */
    infix fun begin(triggerLogicQuery: Query): CompletedTrigger<TModel> =
        CompletedTrigger(this, triggerLogicQuery)

    companion object {

        const val DELETE = "DELETE"
        const val INSERT = "INSERT"
        const val UPDATE = "UPDATE"

        @JvmField
        val METHODS = listOf(INSERT, UPDATE, DELETE)
    }
}
