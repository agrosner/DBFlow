package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Creates an index for a specified {@link Column}. A single column can belong to multiple
 * indexes within the same table if you wish.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Index {

    /**
     * @return The set of index groups that this index belongs to.
     */
    int[] indexGroups() default {};
}
