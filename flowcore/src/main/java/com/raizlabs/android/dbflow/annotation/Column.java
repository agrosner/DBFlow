package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a field as corresponding to a column in the DB.
 * When adding new columns or changing names, you need to define a new {@link com.raizlabs.android.dbflow.annotation.Migration}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * @return The name of the column. The default is the field name.
     */
    String name() default "";

    /**
     * @return An optional column length
     */
    int length() default -1;

    /**
     * @return Marks the field as having a specified collation to use in it's creation.
     */
    Collate collate() default Collate.NONE;

    /**
     * @return Adds a default value for this column when saving
     */
    String defaultValue() default "";

}
