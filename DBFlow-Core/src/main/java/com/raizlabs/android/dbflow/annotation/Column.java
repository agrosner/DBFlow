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

    /**
     * @return If private, by default this is get{Name}() for "name". To define a custom one, this method specifies the name
     * of the method only, and not any specific params. So for "getAnotherName()" you would use "AnotherName" as the param.
     */
    String getterName() default "";

    /**
     * @return If private, by default this is set{Name}() for "name". To define a custom one, this method specifies the name
     * of the method only, and not any specific params. So for "setAnotherName(String name)" you would use "AnotherName" as the param.
     * The params must align exactly to an expected setter, otherwise a compile error ensues.
     */
    String setterName() default "";

    /**
     * @return A custom type converter that's only used for this field. It will be created and used in
     * the Adapter associated with this table.
     */
    Class<? extends com.raizlabs.android.dbflow.converter.TypeConverter> typeConverter() default com.raizlabs.android.dbflow.converter.TypeConverter.class;

}
