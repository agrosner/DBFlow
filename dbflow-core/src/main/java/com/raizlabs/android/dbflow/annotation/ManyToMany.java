package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Description: Builds a many-to-many relationship with another {@link Table}. Only one table needs to specify
 * the annotation and its assumed that they use primary keys only. The generated
 * class will contain an auto-incrementing primary key by default.
 */
@Target(ElementType.TYPE)
public @interface ManyToMany {

    /**
     * @return The other table class by which this will get merged.
     */
    Class<?> referencedTable();

    /**
     * @return By default, we generate an auto-incrementing {@link Long} {@link PrimaryKey}.
     * If false, all {@link PrimaryKey} of the corresponding tables will be placed as {@link ForeignKey} and {@link PrimaryKey}
     * of the generated table instead of using an autoincrementing Long {@link PrimaryKey}.
     */
    boolean generateAutoIncrement() default true;
}
