package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.google.devtools.ksp.symbol.KSFile

/**
 * Description:
 */
class KSPOriginatingSource(val ksFile: KSFile?) : OriginatingSource

fun OriginatingSource.ksFile() = (this as KSPOriginatingSource).ksFile
