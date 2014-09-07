package com.raizlabs.android.dbflow.structure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name();
}
