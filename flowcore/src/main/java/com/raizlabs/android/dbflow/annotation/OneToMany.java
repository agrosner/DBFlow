package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Description: Describes a 1-many relationship. It applies to some method that returns a {@link List} of Model objects.
 * This annotation can handle loading, deleting, and saving when the current data changes. By default it will call the
 * associated method when the
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OneToMany {
    /**
     * The method to apply the OneToMany to.
     */
    enum Method {
        LOAD,
        DELETE,
        SAVE,
        ALL
    }

    /**
     * @return The methods you wish to call it from. By default it's loaded out of the DB.
     */
    Method[] methods() default Method.LOAD;

    /**
     * @return The name of the list variable to use. If is left blank, we will remove the "get" and then decapitalize the remaining name.
     */
    String variableName() default "";
}
