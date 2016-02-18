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
     */
    boolean autoincrement() default false;

    /**
     * Specifies the column to be treated as a ROWID but is not an {@link #autoincrement()}. This
     * overrides {@link #autoincrement()} and is mutually exclusive.
     */
    boolean rowID() default false;

    /**
     * @return When true, we simple do {columnName} &gt; 0 when checking for it's existence if {@link #autoincrement()}
     * is true. If not, we do a full database SELECT exists.
     */
    boolean quickCheckAutoIncrement() default false;
}
