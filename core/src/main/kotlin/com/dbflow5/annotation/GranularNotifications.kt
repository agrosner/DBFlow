package com.dbflow5.annotation

/**
 * Marks a [Table] supporting granular notifications. Otherwise
 * we disable notifications in code generation.
 */
@Target(AnnotationTarget.CLASS)
annotation class GranularNotifications
