package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface UniqueGroup {

    /**
     * @return The number that columns point to to use this group
     */
    int groupNumber();

    /**
     * @return The conflict action that this group takes.
     */
    ConflictAction onUniqueConflict() default ConflictAction.FAIL;
}
