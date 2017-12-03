package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: Provides [Action] for SQL constructs.
 */
interface Actionable {

    val primaryAction: ChangeAction
}
