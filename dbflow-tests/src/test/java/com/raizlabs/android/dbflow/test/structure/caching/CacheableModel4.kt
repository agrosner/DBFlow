package com.raizlabs.android.dbflow.test.structure.caching

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description: Cacheable model.
 */
@Table(database = TestDatabase::class, cachingEnabled = true)
class CacheableModel4 : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0
}
