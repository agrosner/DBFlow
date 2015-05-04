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
    ForeignKeyReference[] references();

    /**
     * @return When this column is a {@link ForeignKey} and a Model, returning true with save the model
     * before adding the fields to save as a foreign key. If false, we expect the field to not change
     * and must save the model manually outside of the ModelAdapter. This also applies to ModelContainer objects
     * as foreign key fields.
     */
    boolean saveForeignKeyModel() default true;

    /**
     * @return an optional table class that this reference points to. It's only used if the field
     * is NOT a Model class.
     */
    Class<?> tableClass() default Void.class;

    /**
     * Defines {@link ForeignKeyAction} action to be performed
     * on delete of referenced record. Defaults to {@link ForeignKeyAction#NO_ACTION}. Used only when
     * columnType is {@link ForeignKey}.
     *
     * @return {@link ForeignKeyAction}
     */
    ForeignKeyAction onDelete() default ForeignKeyAction.NO_ACTION;

    /**
     * Defines {@link ForeignKeyAction} action to be performed
     * on update of referenced record. Defaults to {@link ForeignKeyAction#NO_ACTION}. Used only when
     * columnType is {@link ForeignKey}.
     *
     * @return {@link ForeignKeyAction}
     */
    ForeignKeyAction onUpdate() default ForeignKeyAction.NO_ACTION;
}
