package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = CipherDatabase::class)
class CipherModel : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null
}
