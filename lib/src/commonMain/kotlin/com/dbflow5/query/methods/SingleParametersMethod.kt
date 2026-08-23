package com.dbflow5.query.methods

import com.dbflow5.query.operations.AnyProperty
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method

data class SingleParametersMethod<ReturnType>(
    override val name: String,
) : StandardMethod {
    inline operator fun <reified ReturnType> invoke(
        property: AnyProperty,
    ): Method<ReturnType> =
        method(name, property)
}