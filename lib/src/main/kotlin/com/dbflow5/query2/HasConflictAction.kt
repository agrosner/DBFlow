package com.dbflow5.query2

import com.dbflow5.annotation.ConflictAction

interface HasConflictAction {

    val conflictAction: ConflictAction
}

interface Conflictable<Self : Conflictable<Self>> {
    infix fun or(action: ConflictAction): Self
}


fun <Self : Conflictable<Self>> Conflictable<Self>.orReplace() = or(ConflictAction.REPLACE)
fun <Self : Conflictable<Self>> Conflictable<Self>.orFail() = or(ConflictAction.FAIL)
fun <Self : Conflictable<Self>> Conflictable<Self>.orIgnore() = or(ConflictAction.IGNORE)
fun <Self : Conflictable<Self>> Conflictable<Self>.orRollback() = or(ConflictAction.ROLLBACK)
fun <Self : Conflictable<Self>> Conflictable<Self>.orAbort() = or(ConflictAction.ABORT)


