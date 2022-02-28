package com.dbflow5.annotation

/**
 * Description: Provides ability to add multiple [ManyToMany] annotations at once.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class MultipleManyToMany(vararg val value: ManyToMany)
