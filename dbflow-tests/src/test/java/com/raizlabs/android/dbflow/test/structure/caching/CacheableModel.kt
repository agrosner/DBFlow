package com.raizlabs.android.dbflow.test.structure.caching

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class, cachingEnabled = true, cacheSize = 1000)
class CacheableModel : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null
}
