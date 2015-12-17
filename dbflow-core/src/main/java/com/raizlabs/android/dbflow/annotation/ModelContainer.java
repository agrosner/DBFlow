package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Will generate a $Container class definition for the Model class. It is required when using
 * ModelContainers, that we mark every contained class with this annotation so we can handle them properly.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ModelContainer {

    /**
     * @return True if you want to place a default value for all columns when loading from the Cursor.
     * This overrides {@link ContainerKey#putDefault()} unless its the inverse of this value. So if this is
     * true and {@link ContainerKey#putDefault()} is false, it will not put default for that specified column.
     */
    boolean putDefault() default true;
}
