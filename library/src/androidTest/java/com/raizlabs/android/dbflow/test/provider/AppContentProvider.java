package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@ContentProvider(authority = AppContentProvider.AUTHORITY,
        databaseName = TestDatabase.NAME,
        baseContentUri = AppContentProvider.BASE_CONTENT_URI)
public class AppContentProvider {
    public static final String AUTHORITY = "nz.org.winters.android.nzmobileaccountwidget.provider";
    public static final String BASE_CONTENT_URI = "content://";

    //TwoDegreesExtrasTable

    @TableEndpoint(TwoDegreesExtrasTable.ENDPOINT)
    public static class TwoDegreesExtrasTable {

        public static final String ENDPOINT = "TwoDegreesExtrasTable";

        private static Uri buildUri(String... paths) {
            Uri.Builder builder = Uri.parse(BASE_CONTENT_URI + AUTHORITY).buildUpon();
            for (String path : paths) {
                builder.appendPath(path);
            }
            return builder.build();
        }

        @ContentUri(path = TwoDegreesExtrasTable.ENDPOINT,
                type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        public static Uri CONTENT_URI = buildUri(ENDPOINT);

    }

}
