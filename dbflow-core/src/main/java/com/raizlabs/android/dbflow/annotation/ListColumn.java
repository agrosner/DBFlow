package com.raizlabs.android.dbflow.annotation;

import com.raizlabs.android.dbflow.converter.DefaultListConverter;
import com.raizlabs.android.dbflow.converter.ListConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Describes a {@link Column} that is not really a column, rather a stubbed relationship
 * between the {@link PrimaryKey} of the {@link Table} it's contained in. All operations on the parent
 * table affect the children contained here.
 *
 * @author Andrew Grosner (fuzz)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ListColumn {

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
     * @return Define which class you wish to use as the {@link ListConverter}. By default it just
     * uses the {@link DefaultListConverter}.
     */
    Class<? extends ListConverter> listConverter() default DefaultListConverter.class;
}
