package com.raizlabs.android.dbflow.example

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = ColonyDatabase::class)
class Ant : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var type: String? = null

    @Column
    var isMale: Boolean = false

    @ForeignKey(stubbedRelationship = true)
    var queen: Queen? = null

}