package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import javax.lang.model.element.Element

class KaptOriginatingFileType(
    val element: Element?
) : OriginatingFileType

fun OriginatingFileType.element() = (this as KaptOriginatingFileType).element

