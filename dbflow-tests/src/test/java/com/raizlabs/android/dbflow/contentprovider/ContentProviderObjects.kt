package com.raizlabs.android.dbflow.contentprovider

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel
import com.raizlabs.android.dbflow.structure.provider.BaseSyncableProviderModel
import com.raizlabs.android.dbflow.structure.provider.ContentUtils


/**
 * Description:
 */
@ContentProvider(authority = ContentDatabase.AUTHORITY, database = ContentDatabase::class,
    baseContentUri = ContentDatabase.BASE_CONTENT_URI)
@Database(version = ContentDatabase.VERSION)
object ContentDatabase {

    const val BASE_CONTENT_URI = "content://"

    const val AUTHORITY = "com.raizlabs.android.content.test.ContentDatabase"

    const val VERSION = 1

}

@TableEndpoint(name = ContentProviderModel.NAME, contentProvider = ContentDatabase::class)
@Table(database = ContentDatabase::class, name = ContentProviderModel.NAME)
class ContentProviderModel(@PrimaryKey(autoincrement = true)
                           var id: Long = 0,
                           @Column
                           var notes: String? = null,
                           @Column
                           var title: String? = null) : BaseProviderModel() {

    override fun getDeleteUri() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override fun getInsertUri() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override fun getUpdateUri() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override fun getQueryUri() = TestContentProvider.ContentProviderModel.CONTENT_URI

    companion object {

        const val NAME = "ContentProviderModel"

        @ContentUri(path = NAME, type = ContentUri.ContentType.VND_MULTIPLE + NAME)
        val CONTENT_URI = ContentUtils.buildUriWithAuthority(ContentDatabase.AUTHORITY)
    }
}

@Table(database = ContentDatabase::class)
class NoteModel(@PrimaryKey(autoincrement = true)
                var id: Long = 0,

                @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "providerModel",
                    foreignKeyColumnName = "id")))
                var contentProviderModel: ContentProviderModel? = null,

                @Column
                var note: String? = null,

                @Column
                var isOpen: Boolean = false) : BaseProviderModel() {

    override fun getDeleteUri() = TestContentProvider.NoteModel.CONTENT_URI

    override fun getInsertUri() = TestContentProvider.NoteModel.CONTENT_URI

    override fun getUpdateUri() = TestContentProvider.NoteModel.CONTENT_URI

    override fun getQueryUri() = TestContentProvider.NoteModel.CONTENT_URI
}

@Table(database = ContentDatabase::class)
class TestSyncableModel(@PrimaryKey(autoincrement = true)
                        var id: Long = 0,
                        @Column
                        var name: String? = null) : BaseSyncableProviderModel() {

    override fun getDeleteUri() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override fun getInsertUri() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override fun getUpdateUri() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override fun getQueryUri() = TestContentProvider.TestSyncableModel.CONTENT_URI
}