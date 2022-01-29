package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
interface OriginatingSource {
}

class OriginatingSourceCollection(
    val sources: List<OriginatingSource>,
) : OriginatingSource

interface OriginatingFileTypeSpecAdder {

    fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    ) {
        if (source is OriginatingSourceCollection) {
            addOriginatingFileCollection(typeSpec, source)
        } else {
            addOriginatingFile(typeSpec, source)
        }
    }

    fun addOriginatingFile(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    )

    fun addOriginatingFileCollection(
        typeSpec: TypeSpec.Builder,
        originatingSourceCollection: OriginatingSourceCollection,
    ) {
        originatingSourceCollection.sources.forEach {
            addOriginatingFileType(typeSpec, it)
        }
    }
}
