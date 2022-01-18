package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description: No-op platform fill. Kapt doesn't have quite same.
 */
class KaptOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {
    override fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingSource: OriginatingSource
    ) {
        originatingSource.element()?.let {
            typeSpec.addOriginatingElement(it)
        }
    }
}