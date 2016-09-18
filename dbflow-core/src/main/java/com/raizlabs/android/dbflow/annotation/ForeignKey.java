package com.raizlabs.android.dbflow.annotation;

import com.sun.org.apache.bcel.internal.generic.Select;

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
     * Defines explicit references for a composite {@link ForeignKey} definition. This is no longer required
     * as the library will auto-generate references for you based on the other table's primary keys.
     *
     * @return the set of explicit references if you wish to have different values than default generated.
     */
    ForeignKeyReference[] references() default {};

    /**
     * @return Default false. When this column is a {@link ForeignKey} and a Model, returning true with save the model
     * before adding the fields to save as a foreign key. If false, we expect the field to not change
     * and must save the model manually outside of the ModelAdapter. This also applies to ModelContainer objects
     * as foreign key fields.
     */
    boolean saveForeignKeyModel() default false;

    /**
     * @return Replaces legacy ForeignKeyContainer, this method instructs the code generator to only
     * populate the model with the {@link ForeignKeyReference} defined in this field. This skips
     * the {@link Select} retrieval convenience.
     */
    boolean stubbedRelationship() default false;

    /**
     * @return an optional table class that this reference points to. It's only used if the field
     * is NOT a Model class.
     */
    Class<?> tableClass() default Object.class;

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
