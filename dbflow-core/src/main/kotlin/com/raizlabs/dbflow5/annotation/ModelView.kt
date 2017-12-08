package com.raizlabs.dbflow5.annotation

import com.raizlabs.dbflow5.sql.Query
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
         * @return The class of the database this corresponds to.
         */
        val database: KClass<*>,
        /**
         * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as [com.raizlabs.android.dbflow.annotation.Column] .
         * The only required annotated field becomes The [PrimaryKey]
         * or [PrimaryKey.autoincrement].
         */
        val allFields: Boolean = false,
        /**
         * @return The higher the number, the order by which the creation of this class gets called.
         * Useful for creating ones that depend on another [ModelView].
         */
        val priority: Int = 0)
