package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

/**
 * Placeholder for FIR diagnostics (annotation misuse, missing @Table, etc.).
 * Validation still runs in the shared-model validators during IR generation.
 */
class DBFlowFirCheckersExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session)
