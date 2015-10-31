package com.raizlabs.android.dbflow.annotation;

import com.raizlabs.android.dbflow.sql.Query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Represents a field that is a {@link Query}. This is only meant to be used as a query
 * reference in {@link ModelView}. This is so the annotation processor knows how to access the query of
 * the view.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ModelViewQuery {
}
