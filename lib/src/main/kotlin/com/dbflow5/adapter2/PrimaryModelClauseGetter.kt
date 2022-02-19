package com.dbflow5.adapter2

import com.dbflow5.query.operations.BaseOperator

/**
 * Used by generated code to retrieve model by ids and/or composite keys.
 */
fun interface PrimaryModelClauseGetter<Table : Any> {

    fun get(model: Table): List<BaseOperator.SingleValueOperator<*>>
}