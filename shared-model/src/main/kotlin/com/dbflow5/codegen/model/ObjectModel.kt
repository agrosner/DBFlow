package com.dbflow5.codegen.model

import com.dbflow5.codegen.model.interop.OriginatingFileType

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingFile: OriginatingFileType?
}