package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks a Migration class to be included in DB construction. The class using this annotation
 * must implement the Migration interface.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Migration {

    /**
     * @return The version the migration will trigger at.
     */
    int version();

    /**
     * @return Specify the database class that this migration belongs to.
     */
    Class<?> database();

    /**
     * @return If number greater than -1, the migrations from the same {@link #version()} get ordered from
     * highest to lowest. if they are the same priority, there is no telling which one is executed first. The
     * annotation processor will process in order it finds the classes.
     */
    int priority() default -1;

}
