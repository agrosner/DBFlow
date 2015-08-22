package com.raizlabs.android.dbflow.annotation;

/**
 * Actions associated with on update and on delete
 */
public enum ForeignKeyAction {
    /**
     * When a parent key is modified or deleted from the database, no special action is taken
     */
    NO_ACTION,
    /**
     * The "RESTRICT" action means that the application is prohibited from deleting (for ON DELETE RESTRICT)
     * or modifying (for ON UPDATE RESTRICT) a parent key when there exists one or more child keys mapped to it.
     */
    RESTRICT,
    /**
     * when a parent key is deleted (for ON DELETE SET NULL) or modified (for ON UPDATE SET NULL),
     * the child key columns of all rows in the child table that mapped to the parent key are set
     * to contain SQL NULL values.
     */
    SET_NULL,
    /**
     * The "SET DEFAULT" actions are similar to {@link ForeignKeyAction#SET_NULL}, except that each of the child key
     * columns is set to contain the columns default value instead of NULL
     */
    SET_DEFAULT,
    /**
     * A "CASCADE" action propagates the delete or update operation on the parent key to each dependent child key.
     * For an "ON DELETE CASCADE" action, this means that each row in the child table that was associated with
     * the deleted parent row is also deleted. For an "ON UPDATE CASCADE" action, it means that the values
     * stored in each dependent child key are modified to match the new parent key values.
     */
    CASCADE,
}
