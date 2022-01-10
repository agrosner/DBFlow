package com.dbflow5.model

import com.dbflow5.model.interop.OriginatingFileType

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingFile: OriginatingFileType?
}