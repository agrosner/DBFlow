package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingSource

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingSource: OriginatingSource?
}

interface GeneratedClassModel {
    val generatedClassName: NameModel
}