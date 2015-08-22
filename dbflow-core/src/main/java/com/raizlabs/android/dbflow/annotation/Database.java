package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Creates a new database to use in the application.
 * <p>
 * If we specify one DB, then all models do not need to specify a DB. As soon as we specify two, then each
 * model needs to define what DB it points to.
 * </p>
 * <p>
 * Models will specify which DB it belongs to,
 * but they currently can only belong to one DB.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Database {

    /**
     * @return The current version of the DB. Increment it to trigger a DB update.
     */
    int version();

    /**
     * @return The name of the DB. Optional as it will default to the class name.
     */
    String name() default "";

    /**
     * In order to use Foreign keys, set this to true.
     *
     * @return if key constraints are enabled.
     */
    boolean foreignKeysSupported() default false;

    /**
     * @return Checks for consistency in the DB, if true it will recopy over the prepackage database.
     */
    boolean consistencyCheckEnabled() default false;

    /**
     * @return Keeps a backup for whenever the database integrity fails a "PRAGMA quick_check(1)" that will
     * replace the corrupted DB
     */
    boolean backupEnabled() default false;

    /**
     * @return A custom FlowSQLiteOpenHelper that you can define custom.
     */
    Class<?> sqlHelperClass() default Void.class;

    /**
     * @return Global default insert conflict that can be applied to any table when it leaves
     * its {@link ConflictAction} as NONE.
     */
    ConflictAction insertConflict() default ConflictAction.NONE;

    /**
     * @return Global update conflict that can be applied to any table when it leaves its
     * {@link ConflictAction} as NONE
     */
    ConflictAction updateConflict() default ConflictAction.NONE;

    /**
     * @return Marks all generated classes within this database with this character. For example
     * "TestTable" becomes "TestTable$Table" for a "$" separator.
     */
    String generatedClassSeparator() default "$";
}
