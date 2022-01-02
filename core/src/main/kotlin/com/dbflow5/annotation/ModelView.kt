package com.dbflow5.annotation

import com.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Author: andrewgrosner
 * Description: Marks a class as being an SQL VIEW definition. It must extend BaseModelView and have
 * a single public, static, final field that is annotated with [ModelViewQuery] and be a [Query].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class ModelView(
    /**
     * @return The name of this view. Default is the class name.
     */
    val name: String = "",
    /**
     * @return Specify the database class that this table belongs to.
     * It must have the [Database] annotation. If left blank, the corresponding [Database]
     * should include it.
     */
    val database: KClass<*> = Any::class,
    /**
     * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as [com.dbflow5.annotation.Column] .
     * The only required annotated field becomes The [PrimaryKey]
     * or [PrimaryKey.autoincrement].
     */
    val allFields: Boolean = true,

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
     * @return The higher the number, the order by which the creation of this class gets called.
     * Useful for creating ones that depend on another [ModelView].
     */
    val priority: Int = 0,

    /**
     * @return When false, this view gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    val createWithDatabase: Boolean = true
)
