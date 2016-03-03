package com.raizlabs.android.dbflow.annotation.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Defines a Content Provider that gets generated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ContentProvider {

    /**
     * @return The authority URI for this provider.
     */
    String authority();

    /**
     * @return The class of the database this belongs to
     */
    Class<?> database();

    /**
     * @return The base content uri String to use for all paths
     */
    String baseContentUri() default "";
}
