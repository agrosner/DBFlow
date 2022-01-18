package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingFileType

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingFile: OriginatingFileType?
}