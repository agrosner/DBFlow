package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import com.google.devtools.ksp.symbol.KSFile

/**
 * Description:
 */
class KSPOriginatingFile(val ksFile: KSFile?) : OriginatingFileType

fun OriginatingFileType.ksFile() = (this as KSPOriginatingFile).ksFile


