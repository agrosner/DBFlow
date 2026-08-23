package com.dbflow5.query.methods

import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.InferredObjectConverter
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.SQLValueConverter
import com.dbflow5.query.operations.method

data class AllParametersMethod<ReturnType>(
    override val name: String,
) : StandardMethod {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(
        vararg properties: AnyOperator
    ): Method<ReturnType> =
        method(
            name = name,
            valueConverter = InferredObjectConverter as SQLValueConverter<ReturnType>,
            arguments = properties,
        )
}