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
public @interface ExternalForeignKey {
    /**
     * @return When this column is a {@link ExternalForeignKey} and a Model, returning true with save the model
     * before adding the fields to save as a foreign key. If false, we expect the field to not change
     * and must save the model manually outside of the ModelAdapter. This also applies to ModelContainer objects
     * as foreign key fields.
     */
    boolean saveForeignKeyModel() default true;

    /**
     * @return Required table class that this reference points to.
     */
    Class<?> tableClass() default Object.class;

    /**
     * @return Optional database class that owns the tableClass. This field is necessary if
     * the table is in a database that is not defined in the current application, such as
     * a module.
     */
    Class<?> databaseClass() default Object.class;

    /**
     * Defines explicit references for a composite {@link ExternalForeignKey} definition. This is no required
     * if the table is imported from a module.
     *
     * @return the set of explicit references if you wish to have different values than default generated.
     */
    ForeignKeyReference[] references() default {};
}
