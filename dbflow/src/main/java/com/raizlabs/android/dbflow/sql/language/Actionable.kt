package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.structure.BaseModel.Action

/**
 * Description: Provides [Action] for SQL constructs.
 */
interface Actionable {

    val primaryAction: Action
}
