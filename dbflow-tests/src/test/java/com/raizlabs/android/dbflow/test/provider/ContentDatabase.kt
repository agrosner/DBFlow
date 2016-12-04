package com.raizlabs.android.dbflow.test.provider

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider

/**
 * Description:
 */
@ContentProvider(authority = ContentDatabase.AUTHORITY, database = ContentDatabase::class, baseContentUri = ContentDatabase.BASE_CONTENT_URI)
@Database(version = ContentDatabase.VERSION, name = ContentDatabase.NAME)
object ContentDatabase {

    val BASE_CONTENT_URI = "content://"

    val AUTHORITY = "com.raizlabs.android.content.test.ContentDatabase"

    val NAME = "content"

    val VERSION = 1

}
