package com.raizlabs.android.dbflow.sql.language

import android.content.ContentValues

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.SqlUtils
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description: Used to specify the SET part of an [com.raizlabs.android.dbflow.sql.language.Update] query.
 */
class Set<T : Any>(override val queryBuilderBase: Query, table: Class<T>)
    : BaseTransformable<T>(table), WhereBase<T> {

    private val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause().setAllCommaSeparated(true)

    override val query: String
        get() {
            val queryBuilder = QueryBuilder(queryBuilderBase.query)
                    .append("SET ")
                    .append(operatorGroup.query).appendSpace()
            return queryBuilder.query
        }

    override val primaryAction: BaseModel.Action
        get() = BaseModel.Action.UPDATE

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    fun conditions(vararg conditions: SQLOperator) = apply {
        operatorGroup.andAll(*conditions)
    }

    fun conditionValues(contentValues: ContentValues) = apply {
        SqlUtils.addContentValues(contentValues, operatorGroup)
    }
}
