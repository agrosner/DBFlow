package com.raizlabs.android.dbflow.global

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = GlobalDatabase::class, updateConflict = ConflictAction.IGNORE)
class GlobalModel : BaseModel() {

    @Column
    @PrimaryKey(rowID = true)
    var id: Int = 0

    @Column
    var name: String? = null
}
