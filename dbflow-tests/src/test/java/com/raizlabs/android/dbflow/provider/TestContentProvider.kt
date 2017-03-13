package com.raizlabs.android.dbflow.provider

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.sql.SqlUtils

/**
 * Description:
 */
@ContentProvider(authority = TestContentProvider.AUTHORITY, database = ContentDatabase::class,
    baseContentUri = TestContentProvider.BASE_CONTENT_URI)
object TestContentProvider {

    const val AUTHORITY = "com.raizlabs.android.dbflow.test.provider"

    const val BASE_CONTENT_URI = "content://"

    private fun buildUri(vararg paths: String): Uri {
        val builder = Uri.parse(BASE_CONTENT_URI + AUTHORITY).buildUpon()
        for (path in paths) {
            builder.appendPath(path)
        }
        return builder.build()
    }

    @TableEndpoint(name = ContentProviderModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object ContentProviderModel {

        const val ENDPOINT = "ContentProviderModel"

        @JvmStatic
        @ContentUri(path = ContentProviderModel.ENDPOINT,
            type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        var CONTENT_URI = buildUri(ENDPOINT)

        @JvmStatic
        @ContentUri(path = ContentProviderModel.ENDPOINT + "/#",
            type = ContentUri.ContentType.VND_SINGLE + ENDPOINT,
            segments = arrayOf(ContentUri.PathSegment(segment = 1, column = "id")))
        fun withId(id: Long): Uri {
            return buildUri(id.toString())
        }

        @JvmStatic
        @Notify(method = Notify.Method.INSERT, paths = arrayOf(ContentProviderModel.ENDPOINT + "/#"))
        fun onInsert(contentValues: ContentValues): Array<Uri> {
            val id = contentValues.getAsLong("id")!!
            return arrayOf(withId(id))
        }

    }

    @TableEndpoint(name = NoteModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object NoteModel {

        const val ENDPOINT = "NoteModel"

        @ContentUri(path = ENDPOINT, type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        var CONTENT_URI = buildUri(ENDPOINT)

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#", type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT,
            segments = arrayOf(ContentUri.PathSegment(column = "id", segment = 1)))
        fun withId(id: Long): Uri {
            return buildUri(ENDPOINT, id.toString())
        }

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#/#",
            type = ContentUri.ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
            segments = arrayOf(ContentUri.PathSegment(column = "id", segment = 2)))
        fun fromList(id: Long): Uri {
            return buildUri(ENDPOINT, "fromList", id.toString())
        }

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#/#",
            type = ContentUri.ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
            segments = arrayOf(ContentUri.PathSegment(column = "id", segment = 1),
                ContentUri.PathSegment(column = "isOpen", segment = 2)))
        fun withOpenId(id: Long, isOpen: Boolean): Uri {
            return buildUri(ENDPOINT, id.toString(), isOpen.toString())
        }

        @JvmStatic
        @Notify(method = Notify.Method.INSERT, paths = arrayOf(ENDPOINT))
        fun onInsert(contentValues: ContentValues): Array<Uri> {
            val listId = contentValues.getAsLong(SqlUtils.getContentValuesKey(contentValues, "providerModel"))!!
            return arrayOf(ContentProviderModel.withId(listId), fromList(listId))
        }

        @JvmStatic
        @Notify(method = Notify.Method.INSERT, paths = arrayOf(ENDPOINT))
        fun onInsert2(contentValues: ContentValues): Uri {
            val listId = contentValues.getAsLong(SqlUtils.getContentValuesKey(contentValues, "providerModel"))!!
            return fromList(listId)
        }

        @JvmStatic
        @Notify(method = Notify.Method.UPDATE, paths = arrayOf(ENDPOINT + "/#"))
        fun onUpdate(context: Context, uri: Uri): Array<Uri> {
            val noteId = java.lang.Long.valueOf(uri.pathSegments[1])!!
            val c = context.contentResolver.query(uri, arrayOf("noteModel"), null, null, null)
            c!!.moveToFirst()
            val listId = c.getLong(c.getColumnIndex("providerModel"))
            c.close()

            return arrayOf(withId(noteId), fromList(listId), ContentProviderModel.withId(listId))
        }

        @JvmStatic
        @Notify(method = Notify.Method.DELETE, paths = arrayOf(ENDPOINT + "/#"))
        fun onDelete(context: Context, uri: Uri): Array<Uri> {
            val noteId = java.lang.Long.valueOf(uri.pathSegments[1])!!
            val c = context.contentResolver.query(uri, null, null, null, null)
            c!!.moveToFirst()
            val listId = c.getLong(c.getColumnIndex("providerModel"))
            c.close()

            return arrayOf(withId(noteId), fromList(listId), ContentProviderModel.withId(listId))
        }
    }

    @TableEndpoint(name = TestSyncableModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object TestSyncableModel {

        const val ENDPOINT = "TestSyncableModel"

        @ContentUri(path = ENDPOINT, type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
        var CONTENT_URI = buildUri(ENDPOINT)
    }
}
