package com.raizlabs.android.dbflow.annotation;

/**
 * This is how to resolve null or unique conflicts with a field marked as {@link Column#notNull()}
 * or {@link Column#unique()}
 */
public enum ConflictAction {
    ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
}
