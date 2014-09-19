package com.grosner.dbflow.structure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Marks a {@link com.grosner.dbflow.structure.Model} class as not being counted in the main
 * search for classes at initialization time in
 * {@link com.grosner.dbflow.config.FlowManager#initialize(android.content.Context, com.grosner.dbflow.config.DBConfiguration, com.grosner.dbflow.DatabaseHelperListener)}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
