package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a class as being a TypeConverter. A type converter will turn a non-model, non-SQLiteTyped class into
 * a valid database type.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface TypeConverter {

    /**
     * @return Specify a set of subclasses by which the {@link TypeConverter} registers for. For
     * each one, this will create a new instance of the converter.
     */
    Class<?>[] allowedSubtypes() default {};
}
