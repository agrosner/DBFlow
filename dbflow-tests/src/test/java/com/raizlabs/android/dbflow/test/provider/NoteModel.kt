package com.raizlabs.android.dbflow.test.provider

import android.net.Uri
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel

/**
 * Description:
 */
@Table(database = ContentDatabase::class)
class NoteModel : BaseProviderModel<NoteModel>() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @ForeignKey
    var contentProviderModel: ContentProviderModel? = null

    @Column
    var note: String? = null

    @Column
    var isOpen: Boolean = false

    override fun getDeleteUri(): Uri {
        return TestContentProvider.NoteModel.CONTENT_URI
    }

    override fun getInsertUri(): Uri {
        return TestContentProvider.NoteModel.CONTENT_URI
    }

    override fun getUpdateUri(): Uri {
        return TestContentProvider.NoteModel.CONTENT_URI
    }

    override fun getQueryUri(): Uri {
        return TestContentProvider.NoteModel.CONTENT_URI
    }
}
