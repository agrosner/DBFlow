package com.raizlabs.android.dbflow.test.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.sql.SqlUtils;

/**
 * Description:
 */
@ContentProvider(authority = TestContentProvider.AUTHORITY,
        database = ContentDatabase.class,
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

    @TableEndpoint(name = ContentProviderModel.ENDPOINT, contentProvider = ContentDatabase.class)
    public static class ContentProviderModel {

        public static final String ENDPOINT = "ContentProviderModel";

        @ContentUri(path = ContentProviderModel.ENDPOINT,
                type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        public static Uri CONTENT_URI = buildUri(ENDPOINT);

        @ContentUri(path = ContentProviderModel.ENDPOINT + "/#",
                type = ContentUri.ContentType.VND_SINGLE + ENDPOINT,
                segments = {@ContentUri.PathSegment(segment = 1, column = "id")})
        public static Uri withId(long id) {
            return buildUri(String.valueOf(id));
        }

        @Notify(method = Notify.Method.INSERT,
                paths = ContentProviderModel.ENDPOINT + "/#")
        public static Uri[] onInsert(ContentValues contentValues) {
            final long id = contentValues.getAsLong("id");
            return new Uri[]{
                    withId(id)
            };
        }

    }

    @TableEndpoint(name = NoteModel.ENDPOINT, contentProvider = ContentDatabase.class)
    public static class NoteModel {

        public static final String ENDPOINT = "NoteModel";

        @ContentUri(path = ENDPOINT,
                type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        public static Uri CONTENT_URI = buildUri(ENDPOINT);

        @ContentUri(path = ENDPOINT + "/#",
                type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT,
                segments = {@ContentUri.PathSegment(column = "id", segment = 1)})
        public static Uri withId(long id) {
            return buildUri(ENDPOINT, String.valueOf(id));
        }

        @ContentUri(path = ENDPOINT + "/#/#",
                type = ContentUri.ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
                segments = {@ContentUri.PathSegment(column = "id", segment = 2)})
        public static Uri fromList(long id) {
            return buildUri(ENDPOINT, "fromList", String.valueOf(id));
        }

        @ContentUri(path = ENDPOINT + "/#/#",
                type = ContentUri.ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
                segments = {@ContentUri.PathSegment(column = "id", segment = 1),
                        @ContentUri.PathSegment(column = "isOpen", segment = 2)})
        public static Uri withOpenId(long id, boolean isOpen) {
            return buildUri(ENDPOINT, String.valueOf(id), String.valueOf(isOpen));
        }

        @Notify(method = Notify.Method.INSERT, paths = ENDPOINT)
        public static Uri[] onInsert(ContentValues contentValues) {
            final long listId = contentValues.getAsLong(SqlUtils.getContentValuesKey(contentValues, "providerModel"));
            return new Uri[]{
                    ContentProviderModel.withId(listId), fromList(listId),
            };
        }

        @Notify(method = Notify.Method.INSERT, paths = ENDPOINT)
        public static Uri onInsert2(ContentValues contentValues) {
            final long listId = contentValues.getAsLong(SqlUtils.getContentValuesKey(contentValues, "providerModel"));
            return fromList(listId);
        }

        @Notify(method = Notify.Method.UPDATE, paths = ENDPOINT + "/#")
        public static Uri[] onUpdate(Context context, Uri uri) {
            final long noteId = Long.valueOf(uri.getPathSegments().get(1));
            Cursor c = context.getContentResolver().query(uri, new String[]{
                    "noteModel",
            }, null, null, null);
            c.moveToFirst();
            final long listId = c.getLong(c.getColumnIndex("providerModel"));
            c.close();

            return new Uri[]{
                    withId(noteId), fromList(listId), ContentProviderModel.withId(listId),
            };
        }

        @Notify(method = Notify.Method.DELETE, paths = ENDPOINT + "/#")
        public static Uri[] onDelete(Context context, Uri uri) {
            final long noteId = Long.valueOf(uri.getPathSegments().get(1));
            Cursor c = context.getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();
            final long listId = c.getLong(c.getColumnIndex("providerModel"));
            c.close();

            return new Uri[]{
                    withId(noteId), fromList(listId), ContentProviderModel.withId(listId),
            };
        }
    }

    @TableEndpoint(name = TestSyncableModel.ENDPOINT, contentProvider = ContentDatabase.class)
    public static class TestSyncableModel {

        public static final String ENDPOINT = "TestSyncableModel";

        @ContentUri(path = ENDPOINT,
                type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        public static Uri CONTENT_URI = buildUri(ENDPOINT);
    }
}
