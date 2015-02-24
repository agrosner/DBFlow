package com.raizlabs.android.dbflow.annotation.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotates a method part of {@link com.raizlabs.android.dbflow.annotation.provider.TableEndpoint}
 * that gets called back when changed. The method must return a Uri or an array of Uri[] to notify changed on
 * the content provider.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Notify {

    public enum Method {
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * @return The {@link com.raizlabs.android.dbflow.annotation.provider.Notify.Method} notify
     */
    Method method();

    /**
     * @return Registers itself for the following paths. If a specific path is called for the specified
     * method, the method this annotation corresponds to will be called.
     */
    String[] paths() default {};
}
