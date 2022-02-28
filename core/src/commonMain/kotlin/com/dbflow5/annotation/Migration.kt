package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Marks a Migration class to be included in DB construction. The class using this annotation
 * must implement the Migration interface.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Migration(
    /**
     * @return The version the migration will trigger at.
     */
    val version: Int,
    /**
     * @return Specify the database class that this migration belongs to. If not specified,
     * the [Database] should include it.
     */
    val database: KClass<*> = Any::class,
    /**
     * @return If number greater than -1, the migrations are in run in reverse priority,
     * meaning ones from the same [version] get ordered from
     * lowest to highest number.  if they are the same priority,
     * there is no telling which one is executed first. The
     * annotation processor will process in order it finds the classes.
     */
    val priority: Int = -1
)
