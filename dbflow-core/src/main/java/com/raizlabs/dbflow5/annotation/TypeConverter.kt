package com.raizlabs.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Author: andrewgrosner
 * Description: Marks a class as being a TypeConverter. A type converter will turn a non-model, non-SQLiteTyped class into
 * a valid database type.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class TypeConverter(
        /**
         * @return Specify a set of subclasses by which the [TypeConverter] registers for. For
         * each one, this will create a new instance of the converter.
         */
        val allowedSubtypes: Array<KClass<*>> = arrayOf())
