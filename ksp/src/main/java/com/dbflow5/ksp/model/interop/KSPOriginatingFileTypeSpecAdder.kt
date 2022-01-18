package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description:
 */
class KSPOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {

    override fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingSource: OriginatingSource
    ) {
        originatingSource.ksFile()?.let {
            typeSpec.addOriginatingKSFile(it)
        }
    }
}
