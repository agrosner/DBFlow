package com.dbflow5.query

import android.content.ContentValues
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.addContentValues
import com.dbflow5.sql.Query
import com.dbflow5.structure.ChangeAction

/**
 * Description: Used to specify the SET part of an [com.dbflow5.query.Update] query.
 */
class Set<T : Any> internal constructor(
    override val queryBuilderBase: Query,
    adapter: RetrievalAdapter<T>,
) : BaseTransformable<T>(adapter), WhereBase<T> {

    private val operatorGroup: OperatorGroup =
        OperatorGroup.nonGroupingClause().setAllCommaSeparated(true)

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
    }

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param condition The varg of conditions
     * @return This instance.
     */
    infix fun and(condition: SQLOperator) = apply {
        operatorGroup.and(condition)
    }

    fun conditionValues(contentValues: ContentValues) = apply {
        addContentValues(contentValues, operatorGroup)
    }

    override fun cloneSelf(): Set<T> {
        val set = Set(
            when (queryBuilderBase) {
                is Update<*> -> queryBuilderBase.cloneSelf()
                else -> queryBuilderBase
            }, adapter
        )
        set.operatorGroup.andAll(operatorGroup.conditions)
        return set
    }
}
