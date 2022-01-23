package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.OriginatingSource
import javax.lang.model.element.Element

data class KaptOriginatingSource(
    val element: Element?
) : OriginatingSource

fun OriginatingSource.element() = (this as KaptOriginatingSource).element

