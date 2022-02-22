package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description: No-op platform fill. Kapt doesn't have quite same.
 */
class KaptOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {
    override fun addOriginatingFile(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    ) {
        source.element()?.let {
            typeSpec.addOriginatingElement(it)
        }
    }

    override fun addOriginatingFile(spec: PropertySpec.Builder, source: OriginatingSource) {
        source.element()?.let {
            spec.addOriginatingElement(it)
        }
    }

    override fun addOriginatingFile(spec: FunSpec.Builder, source: OriginatingSource) {
        source.element()?.let {
            spec.addOriginatingElement(it)
        }
    }
}