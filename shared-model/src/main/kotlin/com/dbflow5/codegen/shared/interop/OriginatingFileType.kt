package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
interface OriginatingFileType {
}


interface OriginatingFileTypeSpecAdder {

    fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingFileType: OriginatingFileType)
}
