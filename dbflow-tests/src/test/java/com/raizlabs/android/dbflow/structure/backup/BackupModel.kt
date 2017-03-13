package com.raizlabs.android.dbflow.structure.backup

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = BackupDatabase::class)
class BackupModel : BaseModel() {

    @PrimaryKey
    var name: String? = null
}
