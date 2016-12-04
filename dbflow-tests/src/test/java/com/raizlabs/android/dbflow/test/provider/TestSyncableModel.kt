package com.raizlabs.android.dbflow.test.provider

import android.net.Uri

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.provider.BaseSyncableProviderModel

/**
 * Description:
 */
@Table(database = ContentDatabase::class)
class TestSyncableModel : BaseSyncableProviderModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null

    override fun getDeleteUri(): Uri {
        return TestContentProvider.TestSyncableModel.CONTENT_URI
    }

    override fun getInsertUri(): Uri {
        return TestContentProvider.TestSyncableModel.CONTENT_URI
    }

    override fun getUpdateUri(): Uri {
        return TestContentProvider.TestSyncableModel.CONTENT_URI
    }

    override fun getQueryUri(): Uri {
        return TestContentProvider.TestSyncableModel.CONTENT_URI
    }
}
