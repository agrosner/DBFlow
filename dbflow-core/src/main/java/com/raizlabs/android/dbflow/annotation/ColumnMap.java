package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Maps an arbitrary object and its corresponding fields into a set of columns. It is similar
 * to {@link ForeignKey} except it's not represented in the DB hierarchy.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface ColumnMap {

    /**
     * Defines explicit references for a composite {@link ColumnMap} definition.
     *
     * @return override explicit usage of all fields and provide custom references.
     */
    ColumnMapReference[] references() default {};
}
