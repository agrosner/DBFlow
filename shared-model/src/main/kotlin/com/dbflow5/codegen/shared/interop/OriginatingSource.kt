package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
interface OriginatingSource {
}


interface OriginatingFileTypeSpecAdder {

    fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingSource: OriginatingSource)
}
