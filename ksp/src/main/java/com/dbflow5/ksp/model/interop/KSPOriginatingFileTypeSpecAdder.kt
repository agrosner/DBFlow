package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description:
 */
class KSPOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {

    override fun addOriginatingFile(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    ) {
        source.ksFile()?.let {
            typeSpec.addOriginatingKSFile(it)
        }
    }

    override fun addOriginatingFile(spec: PropertySpec.Builder, source: OriginatingSource) {
        source.ksFile()?.let {
            spec.addOriginatingKSFile(it)
        }
    }

    override fun addOriginatingFile(spec: FunSpec.Builder, source: OriginatingSource) {
        source.ksFile()?.let {
            spec.addOriginatingKSFile(it)
        }
    }
}
