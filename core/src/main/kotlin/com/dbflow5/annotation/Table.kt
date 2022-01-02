package com.dbflow5.annotation

import kotlin.reflect.KClass

val DEFAULT_CACHE_SIZE = 25

/**
 * Author: andrewgrosner
 * Description: Marks a class as being a table for only ONE DB. It must implement the Model interface and all fields MUST be package private.
 * This will generate a $Table and $Adapter class. The $Table class generates static final column name variables to reference in queries.
 * The $Adapter class defines how to retrieve and store this object as well as other methods for acting on model objects in the database.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Table(
    /**
     * @return Specifies a different name for the table than the name of the Model class.
     */
    val name: String = "",
    /**
     * @return Specify the database class that this table belongs to.
     * It must have the [Database] annotation. If left blank, the corresponding [Database]
     * should include it.
     */
    val database: KClass<*> = Any::class,
    /**
     * @return Specify the general conflict algorithm used by this table when updating records.
     */
    val updateConflict: ConflictAction = ConflictAction.NONE,
    /**
     * @return Specify the general insert conflict algorithm used by this table.
     */
    val insertConflict: ConflictAction = ConflictAction.NONE,
    /**
     * @return An optional [ConflictAction] that we append to creation for conflict handling in PK.
     */
    val primaryKeyConflict: ConflictAction = ConflictAction.NONE,
    /**
     * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as [com.dbflow5.annotation.Column] .
     * The only required annotated field becomes The [PrimaryKey]
     * or [PrimaryKey.autoincrement].
     */
    val allFields: Boolean = true,
    /**
     * @return If true, all private boolean fields will use "is" instead of "get" for its getter and
     * "set" without the "is" if it starts with "is"
     */
    @Deprecated(
        "This is used for KAPT java generation. in KSP, this is not " +
            "used anymore."
    )
    val useBooleanGetterSetters: Boolean = true,
    /**
     * @return If true, we throw away checks for column indexing and simply assume that the cursor returns
     * all our columns in order. This may provide a slight performance boost.
     */
    val orderedCursorLookUp: Boolean = false,
    /**
     * @return When true, we reassign the corresponding Model's fields to default values when loading
     * from cursor. If false, we assign values only if present in Cursor.
     */
    val assignDefaultValuesFromCursor: Boolean = true,
    /**
     * @return When false, this table gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    val createWithDatabase: Boolean = true,

    /**
     * If true this table will be created as a TEMP table. Pair this with [createWithDatabase]
     * to properly not create the table.
     */
    val temporary: Boolean = false,

    /**
     * @return Declares a set of UNIQUE columns with the corresponding [ConflictAction]. A [Column]
     * will point to this group using [Unique.uniqueGroups]
     */
    val uniqueColumnGroups: Array<UniqueGroup> = [],
    /**
     * @return The set of INDEX clauses that specific columns can define to belong to, using the [Index] annotation.
     * The generated Index properties belong to the corresponding property class to this table.
     */
    val indexGroups: Array<IndexGroup> = [],
    /**
     * @return A set of inherited accessible fields not necessarily defined as columns in the super class of this table.
     * Each must be accessible via: public, package private, or protected or getter/setters.
     */
    @Deprecated("use @ColumnMap fields instead.")
    val inheritedColumns: Array<InheritedColumn> = [],
    /**
     * @return A set of inherited accessible fields not necessarily defined as columns in the super class of this table.
     * Each must be accessible via: public, package private, or protected or getter/setters.
     */
    @Deprecated("use @ColumnMap fields instead.")
    val inheritedPrimaryKeys: Array<InheritedPrimaryKey> = []
)
