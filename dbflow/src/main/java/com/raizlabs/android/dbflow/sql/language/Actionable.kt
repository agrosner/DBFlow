package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.structure.ChangeAction

/**
 * Description: Provides [Action] for SQL constructs.
 */
interface Actionable {

    val primaryAction: ChangeAction
}
