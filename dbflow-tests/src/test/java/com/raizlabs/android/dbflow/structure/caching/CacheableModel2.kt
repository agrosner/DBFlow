package com.raizlabs.android.dbflow.structure.caching

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class, cachingEnabled = true)
class CacheableModel2 : BaseModel() {

    @Column
    @PrimaryKey
    var id: Int = 0
}
