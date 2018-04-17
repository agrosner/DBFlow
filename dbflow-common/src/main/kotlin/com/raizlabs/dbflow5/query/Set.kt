package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: Used to specify the SET part of an [com.raizlabs.dbflow5.query.Update] query.
 * This is an internal class that is subclassed by the platform specific implementations. Should not be used directly.
 */
abstract class InternalSet<T : Any> internal constructor(
    override val queryBuilderBase: Query, table: KClass<T>)
    : BaseTransformable<T>(table), WhereBase<T> {

    protected val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause().setAllCommaSeparated(true)

    override val query: String
        get() = " ${queryBuilderBase.query}SET ${operatorGroup.query} "

    override val primaryAction: ChangeAction
        get() = ChangeAction.UPDATE

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    fun conditions(vararg conditions: SQLOperator) = apply {
        operatorGroup.andAll(*conditions)

    } as Set<T>

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param condition The varg of conditions
     * @return This instance.
     */
    infix fun and(condition: SQLOperator) = apply {
        operatorGroup.and(condition)
    } as Set<T>

    override fun cloneSelf(): Set<T> {
        val base = queryBuilderBase
        val set = Set(
            when (base) {
                is Update<*> -> base.cloneSelf()
                else -> base
            }, table)
        set.operatorGroup.andAll(operatorGroup.conditions)
        return set
    }
}

expect class Set<T : Any> internal constructor(queryBuilderBase: Query, table: KClass<T>) : InternalSet<T>
