package com.dbflow5.annotation

/**
 * Description: Marks a particular feature as being only available in KSP.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING
)
annotation class DBFlowKSPOnly
