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

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = Uri.parse(BASE_CONTENT_URI + AUTHORITY).buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(ContentProviderModel.ENDPOINT)
    public static class ContentProviderModel {

        public static final String ENDPOINT = "ContentProviderModel";

        @ContentUri(path = ContentProviderModel.ENDPOINT,
            type = "vnd.android.cursor.dir/" + ENDPOINT)
        public static Uri CONTENT_URI = buildUri(ENDPOINT);

        @ContentUri(path = ContentProviderModel.ENDPOINT + "/#",
        type = "vnd.android.cursor.item/" + ENDPOINT,
        segments = {@ContentUri.PathSegment(segment = 1, column = "id")})
        public static Uri withId(long id) {
            return buildUri(String.valueOf(id));
        }

    }
}
