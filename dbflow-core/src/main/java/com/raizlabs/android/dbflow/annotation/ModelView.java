package com.raizlabs.android.dbflow.annotation;

import com.raizlabs.android.dbflow.sql.Query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a class as being an SQL VIEW definition. It must extend BaseModelView and have
 * a single public, static, final field that is annotated with {@link ModelViewQuery} and be a {@link Query}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ModelView {

    /**
     * @return The name of this view. Default is the class name.
     */
    String name() default "";

    /**
     * @return The class of the database this corresponds to.
     */
    Class<?> database();


    /**
     * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as {@link com.raizlabs.android.dbflow.annotation.Column} .
     * The only required annotated field becomes The {@link PrimaryKey}
     * or {@link PrimaryKey#autoincrement()}.
     */
    boolean allFields() default false;

    /**
     * @return The higher the number, the order by which the creation of this class gets called.
     * Useful for creating ones that depend on another {@link ModelView}.
     */
    int priority() default 0;
}
