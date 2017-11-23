package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.quoteIfNeeded
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty
import com.raizlabs.android.dbflow.structure.BaseModel

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
(private val indexProperty: IndexProperty<TModel>, private val whereBase: WhereBase<TModel>)
    : BaseTransformable<TModel>(whereBase.table) {

    override val queryBuilderBase: Query
        get() = whereBase.queryBuilderBase

    override val query: String
        get() = buildString {
            append(whereBase.query)
            append(" INDEXED BY ")
            append(indexProperty.indexName.quoteIfNeeded())
            append(" ")
        }

    override val primaryAction: BaseModel.Action
        get() = whereBase.primaryAction
}
