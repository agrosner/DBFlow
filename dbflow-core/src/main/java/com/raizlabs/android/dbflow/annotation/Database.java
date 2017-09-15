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
     * @deprecated use DatabaseConfig.databaseName() to change the name.
     */
    @Deprecated
    String name() default "";

    /**
     * @deprecated use DatabaseConfig.extension() to change the extension.
     */
    @Deprecated
    String databaseExtension() default "";

    /**
     * @deprecated use DatabaseConfig.inMemoryBuilder() instead.
     */
    @Deprecated
    boolean inMemory() default false;

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
     * @deprecated Generated class files will become '_' only in next major release.
     */
    @Deprecated
    String generatedClassSeparator() default "_";
}
