package com.dbflow5.query

import com.dbflow5.query.property.IndexProperty
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query
import com.dbflow5.structure.ChangeAction

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
class IndexedBy<TModel : Any>
/**
 * Creates the INDEXED BY part of the clause.
 *
 * @param indexProperty The index property generated.
 * @param whereBase     The base piece of this query
 */
    (
    private val indexProperty: IndexProperty<TModel>,
    private val whereBase: WhereBase<TModel>
) : BaseTransformable<TModel>(whereBase.adapter) {

    override val queryBuilderBase: Query
        get() = whereBase.queryBuilderBase

    override val query: String
        get() = buildString {
            append(whereBase.query)
            append(" INDEXED BY ")
            append(indexProperty.indexName.quoteIfNeeded())
            append(" ")
        }

    override val primaryAction: ChangeAction
        get() = whereBase.primaryAction

    override fun cloneSelf(): IndexedBy<TModel> = IndexedBy(indexProperty, whereBase.cloneSelf())
}
