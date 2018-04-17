package com.raizlabs.dbflow5.sql

import com.raizlabs.dbflow5.JvmStatic

/**
 * This is how to resolve null or unique conflicts with a field marked as [NotNull]
 * or [Unique]
 */
enum class ConflictAction {

    /**
     * Do nothing and do not specify any algorithm.
     */
    NONE,

    /**
     * When an applicable constraint violation occurs, the ROLLBACK resolution algorithm aborts the
     * current SQL statement with an SQLITE_CONSTRAINT error and rolls back the current transaction.
     * If no transaction is active (other than the implied transaction that is created on every command)
     * then the ROLLBACK resolution algorithm works the same as the [.ABORT] algorithm.
     */
    ROLLBACK,

    /**
     * When an applicable constraint violation occurs, the ABORT resolution algorithm aborts the current
     * SQL statement with an SQLITE_CONSTRAINT error and backs out any changes made by the current SQL statement;
     * but changes caused by prior SQL statements within the same transaction are preserved and
     * the transaction remains active. This is the default behavior and the behavior specified by the SQL standard.
     */
    ABORT,

    /**
     * When an applicable constraint violation occurs, the FAIL resolution algorithm aborts the current
     * SQL statement with an SQLITE_CONSTRAINT error. But the FAIL resolution does not back out prior
     * changes of the SQL statement that failed nor does it end the transaction. For example,
     * if an UPDATE statement encountered a constraint violation on the 100th row that it attempts to update,
     * then the first 99 row changes are preserved but changes to rows 100 and beyond never occur.
     */
    FAIL,

    /**
     * When an applicable constraint violation occurs, the IGNORE resolution algorithm skips the
     * one row that contains the constraint violation and continues processing subsequent rows of the
     * SQL statement as if nothing went wrong. Other rows before and after the row that contained
     * the constraint violation are inserted or updated normally. No error is returned when the IGNORE
     * conflict resolution algorithm is used.
     */
    IGNORE,

    /**
     * When a UNIQUE or PRIMARY KEY constraint violation occurs,
     * the REPLACE algorithm deletes pre-existing rows that are causing the constraint violation prior
     * to inserting or updating the current row and the command continues executing normally.
     * If a NOT NULL constraint violation occurs, the REPLACE conflict resolution replaces the NULL value
     * with the default value for that column, or if the column has no default value,
     * then the ABORT algorithm is used.
     * If a CHECK constraint violation occurs, the REPLACE conflict resolution algorithm always works like ABORT.
     * When the REPLACE conflict resolution strategy deletes rows in order to satisfy a constraint,
     * delete triggers fire if and only if recursive triggers are enabled.
     * The update hook is not invoked for rows that are deleted by the REPLACE conflict resolution strategy.
     * Nor does REPLACE increment the change counter.
     */
    REPLACE;


    companion object {

        @JvmStatic
        fun getSQLiteDatabaseAlgorithmInt(conflictAction: ConflictAction) =
                when (conflictAction) {
                    ROLLBACK -> 1
                    ABORT -> 2
                    FAIL -> 3
                    IGNORE -> 4
                    REPLACE -> 5
                    else -> 0
                }
    }
}
