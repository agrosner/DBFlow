package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@ContentProvider(authority = TestContentProvider.AUTHORITY,
        databaseName = TestDatabase.NAME,
        baseContentUri = TestContentProvider.BASE_CONTENT_URI)
public class TestContentProvider {

    public static final String AUTHORITY = "com.raizlabs.android.dbflow.test.provider";

    public static final String BASE_CONTENT_URI = "content://";

    @TableEndpoint("TestModel1")
    public static class TestModel1 {

        @ContentUri(endpoint = "testmodel1",
            type = "vnd.android.cursor.dir/testmodel1")
        public static Uri CONTENT_URI;
    }
}
