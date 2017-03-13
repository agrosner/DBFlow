package com.raizlabs.android.dbflow.provider

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider

/**
 * Description:
 */
@ContentProvider(authority = ContentDatabase.AUTHORITY, database = ContentDatabase::class,
    baseContentUri = ContentDatabase.BASE_CONTENT_URI)
@Database(version = ContentDatabase.VERSION, name = ContentDatabase.NAME)
object ContentDatabase {

    const val BASE_CONTENT_URI = "content://"

    const val AUTHORITY = "com.raizlabs.android.content.test.ContentDatabase"

    const val NAME = "content"

    const val VERSION = 1

}
