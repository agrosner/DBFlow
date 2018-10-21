package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Marks a Migration class to be included in DB construction. The class using this annotation
 * must implement the Migration interface.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Migration(
        /**
         * @return The version the migration will trigger at.
         */
        val version: Int,
        /**
         * @return Specify the database class that this migration belongs to.
         */
        val database: KClass<*>,
        /**
         * @return If number greater than -1, the migrations from the same [.version] get ordered from
         * highest to lowest. if they are the same priority, there is no telling which one is executed first. The
         * annotation processor will process in order it finds the classes.
         */
        val priority: Int = -1)
