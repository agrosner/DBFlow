package com.dbflow5.query

import com.dbflow5.structure.ChangeAction

/**
 * Description: Provides [Action] for SQL constructs.
 */
interface Actionable {

    val primaryAction: ChangeAction
}
