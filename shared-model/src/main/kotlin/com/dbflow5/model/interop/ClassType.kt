package com.dbflow5.model.interop

import com.squareup.kotlinpoet.TypeName

/**
 * Description: Analog to KSType in KSP.
 */
interface ClassType {

    fun makeNotNullable(): ClassType

    val declaration: Declaration

    fun toTypeName(): TypeName

    val isMarkedNullable: Boolean
}
