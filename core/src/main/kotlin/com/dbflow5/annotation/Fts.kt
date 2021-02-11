package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Creates a class using the SQLITE FTS3 [https://www.sqlite.org/fts3.html]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Fts3


/**
 * Description: Creates a class using the SQLITE FTS4 [https://www.sqlite.org/fts3.html]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Fts4(
    /**
     * Optionally points to a content table that fills this FTS4 with content.
     * The content option allows FTS4 to forego storing the text being indexed and
     * results in significant space savings.
     */
    val contentTable: KClass<*> = Any::class
)

