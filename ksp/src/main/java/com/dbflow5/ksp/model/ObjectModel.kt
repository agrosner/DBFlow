package com.dbflow5.ksp.model

import com.google.devtools.ksp.symbol.KSFile

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingFile: KSFile?
}