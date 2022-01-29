package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.interop.OriginatingSource
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
}
