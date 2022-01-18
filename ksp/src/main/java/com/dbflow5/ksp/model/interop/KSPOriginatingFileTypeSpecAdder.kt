package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description:
 */
class KSPOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {

    override fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        originatingFileType: OriginatingFileType
    ) {
        originatingFileType.ksFile()?.let {
            typeSpec.addOriginatingKSFile(it)
        }
    }
}
