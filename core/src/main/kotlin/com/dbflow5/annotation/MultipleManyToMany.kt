package com.dbflow5.annotation

/**
 * Description: Provides ability to add multiple [ManyToMany] annotations at once.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class MultipleManyToMany(vararg val value: ManyToMany)
