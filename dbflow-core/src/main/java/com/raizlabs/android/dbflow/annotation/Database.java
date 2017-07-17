package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
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
     * @return The name of the FileName DB. Optional as it will default to the class name.
     */
    String fileName() default "";

    /**
     * @return Specify the extension of the file name : {fileName}.{extension}. Default is ".db"
     */
    String databaseExtension() default "";

    /**
     * @return If true, SQLite will throw exceptions when {@link ForeignKey} constraints are not respected.
     * Default is false and will not throw exceptions.
     */
    boolean foreignKeyConstraintsEnforced() default false;

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
     * @return true if you want it to be in-memory, false if not.
     */
    boolean inMemory() default false;

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
    String generatedClassSeparator() default "_";
}
