package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description: No-op platform fill. Kapt doesn't have quite same.
 */
class KaptOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {
    override fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingFileType: OriginatingFileType
    ) {
        originatingFileType.element()?.let {
            typeSpec.addOriginatingElement(it)
        }
    }
}