package com.dbflow5.adapter2

import kotlin.reflect.KClass

/**
 * Description:
 */
sealed interface QueryRepresentable<QueryType : Any> : QueryOps<QueryType> {
    /**
     * Type in the DB it is.
     */
    val type: KClass<QueryType>
}