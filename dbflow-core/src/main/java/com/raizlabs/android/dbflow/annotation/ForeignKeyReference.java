package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: andrewgrosner
 * Description: Used inside of {@link com.raizlabs.android.dbflow.annotation.ForeignKey#references()}, describes the
 * local column name, type, and referencing table column name. Note: the type of the local column must match the
 * column type of the referenced column. By using a field as a Model object, we will ensure the same types are used. In future
 * versions I hope to remove the columnType and directly use the foreign column's type.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ForeignKeyReference {

    /**
     * @return The local column name that will be referenced in the DB
     */
    String columnName();

    /**
     * Needs to match both tables!
     *
     * @return The type of columns between tables
     */
    Class<?> columnType();

    /**
     * @return The column name in the referenced table
     */
    String foreignKeyColumnName();

    /**
     * @return True here if the referenced field is private. It must have a getter with the same name available such
     * that a field "name" has "getName()".
     */
    boolean referencedFieldIsPrivate() default false;

    /**
     * @return sets the name of the referenced field getter.
     * @see Column#getterName() for more information.
     */
    String referencedGetterName() default "";

    /**
     * @return Sets the name of the referenced field setter.
     * @see Column#setterName() for more information.
     */
    String referencedSetterName() default "";
}
