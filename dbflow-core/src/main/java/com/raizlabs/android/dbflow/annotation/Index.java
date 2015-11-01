package com.raizlabs.android.dbflow.annotation;

/**
 * Description: Creates an index for a specified {@link Column}. A single column can belong to multiple
 * indexes within the same table if you wish.
 */
public @interface Index {

    /**
     * @return The set of index groups that this index belongs to.
     */
    int[] indexGroups() default {};
}
