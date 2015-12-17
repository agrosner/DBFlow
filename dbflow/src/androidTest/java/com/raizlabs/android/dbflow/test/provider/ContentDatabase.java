package com.raizlabs.android.dbflow.test.provider;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;

/**
 * Description:
 */
@ContentProvider(authority = ContentDatabase.AUTHORITY,
        database = ContentDatabase.class,
        baseContentUri = ContentDatabase.BASE_CONTENT_URI)
@Database(version = ContentDatabase.VERSION,
        name = ContentDatabase.NAME)
public class ContentDatabase {

    public static final String BASE_CONTENT_URI = "content://";

    public static final String AUTHORITY = "com.raizlabs.android.content.test.ContentDatabase";

    public static final String NAME = "content";

    public static final int VERSION = 1;

}
