package com.raizlabs.android.dbflow.sql.language

import android.content.ContentValues
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.addContentValues
import com.raizlabs.android.dbflow.structure.ChangeAction
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Used to specify the SET part of an [com.raizlabs.android.dbflow.sql.language.Update] query.
 */
class Set<T : Any> internal constructor(
        databaseWrapper: DatabaseWrapper,
        override val queryBuilderBase: Query, table: Class<T>)
    : BaseTransformable<T>(databaseWrapper, table), WhereBase<T> {

    private val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause().setAllCommaSeparated(true)

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
}
