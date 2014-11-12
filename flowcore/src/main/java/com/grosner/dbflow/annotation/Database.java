package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Creates a new database to use in the application.
 *
 * If we specify one DB, then all models do not need to specify a DB. As soon as we specify two, then each
 * model needs to define what DB it points to.
 *
 * Models will specify which DB it belongs to,
 * but they currently can only belong to one DB.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Database {

    /**
     * The current version of the DB. Increment it to trigger a DB update.
     * @return
     */
    int version();

    /**
     * The name of the DB. Optional as it will default to the class name.
     * @return
     */
    String name() default "";

    /**
     * In order to use Foreign keys, set this to true.
     * @return
     */
    boolean foreignKeysSupported() default false;

    /**
     * Checks for consistency in the DB, if true it will recopy over the prepackage database.
     * @return
     */
    boolean consistencyCheckEnabled() default false;
}
