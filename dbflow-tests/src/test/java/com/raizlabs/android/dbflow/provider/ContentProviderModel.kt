package com.raizlabs.android.dbflow.provider

import android.net.Uri

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel
import com.raizlabs.android.dbflow.structure.provider.ContentUtils

/**
 * Description:
 */
@TableEndpoint(name = ContentProviderModel.NAME, contentProvider = ContentDatabase::class)
@Table(database = ContentDatabase::class, name = ContentProviderModel.NAME)
class ContentProviderModel : BaseProviderModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var notes: String? = null

    @Column
    var title: String? = null

    override fun getDeleteUri(): Uri {
        return TestContentProvider.ContentProviderModel.CONTENT_URI
    }

    override fun getInsertUri(): Uri {
        return TestContentProvider.ContentProviderModel.CONTENT_URI
    }

    override fun getUpdateUri(): Uri {
        return TestContentProvider.ContentProviderModel.CONTENT_URI
    }

    override fun getQueryUri(): Uri {
        return TestContentProvider.ContentProviderModel.CONTENT_URI
    }

    companion object {

        const val NAME = "ContentProviderModel"

        @ContentUri(path = NAME, type = ContentUri.ContentType.VND_MULTIPLE + NAME)
        val CONTENT_URI = ContentUtils.buildUriWithAuthority(ContentDatabase.AUTHORITY)
    }
}
