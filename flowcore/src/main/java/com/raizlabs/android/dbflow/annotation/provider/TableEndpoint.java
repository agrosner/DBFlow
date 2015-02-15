package com.raizlabs.android.dbflow.annotation.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Defines an endpoint that gets placed inside of a {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TableEndpoint {

    /**
     * @return The name of the table this endpoint corresponds to.
     */
    String value();
}
