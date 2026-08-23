package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Defines the originating type or file that created the generated code.
 * useful for incremental code gen.
 */
interface OriginatingSource

class OriginatingSourceCollection(
    val sources: List<OriginatingSource>,
) : OriginatingSource

interface OriginatingFileTypeSpecAdder {

    fun addOriginatingFileType(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    ) {
        if (source is OriginatingSourceCollection) {
            source.sources.forEach {
                addOriginatingFileType(typeSpec, it)
            }
        } else {
            addOriginatingFile(typeSpec, source)
        }
    }

    fun addOriginatingFileType(
        propertySpec: PropertySpec.Builder,
        source: OriginatingSource
    ) {
        if (source is OriginatingSourceCollection) {
            source.sources.forEach {
                addOriginatingFileType(propertySpec, it)
            }
        } else {
            addOriginatingFile(propertySpec, source)
        }
    }

    fun addOriginatingFileType(
        funSpec: FunSpec.Builder,
        source: OriginatingSource,
    ) {
        if (source is OriginatingSourceCollection) {
            source.sources.forEach {
                addOriginatingFileType(funSpec, it)
            }
        } else {
            addOriginatingFile(funSpec, source)
        }
    }

    fun addOriginatingFile(
        typeSpec: TypeSpec.Builder,
        source: OriginatingSource
    )

    fun addOriginatingFile(
        spec: PropertySpec.Builder,
        source: OriginatingSource
    )

    fun addOriginatingFile(
        spec: FunSpec.Builder,
        source: OriginatingSource,
    )
}
