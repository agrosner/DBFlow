package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface ForeignKey {

    /**
     * Defines the references for a composite {@link ForeignKey} definition. It enables for multiple local
     * columns that reference another Model's primary keys.
     *
     * @return the set of references
     */
    ForeignKeyReference[] references() default {};

    /**
     * @return When this column is a {@link ForeignKey} and a Model, returning true with save the model
     * before adding the fields to save as a foreign key. If false, we expect the field to not change
     * and must save the model manually outside of the ModelAdapter. This also applies to ModelContainer objects
     * as foreign key fields.
     */
    boolean saveForeignKeyModel() default true;
}
