package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks a Model class as NOT a {@link Table}, but generates code for retrieving data from a
 * generic query
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface QueryModel {

    /**
     * @return Specify the name of the database to use.
     */
    String databaseName();

    /**
     * @return If true, all accessible, non-static, and non-final fields are treated as columns.
     * @see Table#allFields()
     */
    boolean allFields() default false;
}
