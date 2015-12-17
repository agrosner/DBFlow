package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a class as being a table for only ONE DB. It must implement the Model interface and all fields MUST be package private.
 * This will generate a $Table and $Adapter class. The $Table class generates static final column name variables to reference in queries.
 * The $Adapter class defines how to retrieve and store this object as well as other methods for acting on model objects in the database.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Table {

    int DEFAULT_CACHE_SIZE = 25;

    /**
     * @return Specifies a different name for the table than the name of the Model class.
     */
    String name() default "";

    /**
     * @return Specify the database class that this table belongs to. It must have the {@link Database} annotation.
     */
    Class<?> database();

    /**
     * @return Specify the general conflict algorithm used by this table when updating records.
     */
    ConflictAction updateConflict() default ConflictAction.NONE;

    /**
     * @return Specify the general insert conflict algorithm used by this table.
     */
    ConflictAction insertConflict() default ConflictAction.NONE;

    /**
     * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as {@link com.raizlabs.android.dbflow.annotation.Column} .
     * The only required annotated field becomes The {@link com.raizlabs.android.dbflow.annotation.PrimaryKey}
     * or {@link PrimaryKey#autoincrement()}.
     */
    boolean allFields() default false;

    /**
     * @return If true, all private boolean fields will use "is" instead of "get" for its getter.
     */
    boolean useIsForPrivateBooleans() default false;

    /**
     * @return If true, caching mechanism is enabled. This works for single primary key tables. For
     * multi-primary key tables, IMultiKeyCacheModel interface is required to specify the caching key.
     */
    boolean cachingEnabled() default false;

    /**
     * @return The cache size for this Table.
     */
    int cacheSize() default 25;

    /**
     * @return Declares a set of UNIQUE columns with the corresponding {@link ConflictAction}. A {@link Column}
     * will point to this group using {@link Unique#uniqueGroups()}
     */
    UniqueGroup[] uniqueColumnGroups() default {};

    /**
     * @return The set of INDEX clauses that specific columns can define to belong to, using the {@link Index} annotation.
     * The generated Index properties belong to the corresponding property class to this table.
     */
    IndexGroup[] indexGroups() default {};

    /**
     * @return A set of inherited accessible fields not necessarily defined as columns in the super class of this table.
     * Each must be accessible via: public, package private, or protected or getter/setters.
     */
    InheritedColumn[] inheritedColumns() default {};

    /**
     * @return A set of inherited accessible fields not necessarily defined as columns in the super class of this table.
     * Each must be accessible via: public, package private, or protected or getter/setters.
     */
    InheritedPrimaryKey[] inheritedPrimaryKeys() default {};

}
