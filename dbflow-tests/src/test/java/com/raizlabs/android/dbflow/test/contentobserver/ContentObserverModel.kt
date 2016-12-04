package com.raizlabs.android.dbflow.test.contentobserver

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ContentObserverModel : BaseModel() {

    @Column
    @PrimaryKey
    var name: String = ""

    @Column
    @PrimaryKey
    var id: Int = 0

    @Column
    var somethingElse: String = ""

}
