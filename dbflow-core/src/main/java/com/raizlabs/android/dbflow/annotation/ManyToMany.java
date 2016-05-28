package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Builds a many-to-many relationship with another {@link Table}. Only one table needs to specify
 * the annotation and its assumed that they use primary keys only. The generated
 * class will contain an auto-incrementing primary key by default.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ManyToMany {

    /**
     * @return The other table class by which this will get merged.
     */
    Class<?> referencedTable();

    /**
     * @return A name that we use as the column name for the referenced table in the
     * generated ManyToMany table class.
     */
    String referencedTableColumnName() default "";

    /**
     * @return A name that we use as the column name for this specific table's name.
     */
    String thisTableColumnName() default "";

    /**
     * @return By default, we generate an auto-incrementing {@link Long} {@link PrimaryKey}.
     * If false, all {@link PrimaryKey} of the corresponding tables will be placed as {@link ForeignKey} and {@link PrimaryKey}
     * of the generated table instead of using an autoincrementing Long {@link PrimaryKey}.
     */
    boolean generateAutoIncrement() default true;

    /**
     * @return by default, we append {selfTable}{generatedClassSeparator}{referencedTable} or "User_Follower",
     * for example. If you want different name, change this.
     */
    String generatedTableClassName() default "";

    /**
     * @return by default the Models referenced here are not saved prior to saving this
     * object for obvious efficiency reasons.
     * @see ForeignKey#saveForeignKeyModel()
     */
    boolean saveForeignKeyModels() default false;
}
