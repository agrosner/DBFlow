package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

    /**
     * Specifies if the column is autoincrementing or not
     *
     * @return the columnType int
     */
    boolean autoincrement() default false;

    /**
     * @return When true, we simple do {columnName} > 0 when checking for it's existence if {@link #autoincrement()}
     * is true. If not, we do a full database SELECT exists.
     */
    boolean quickCheckAutoIncrement() default false;
}
