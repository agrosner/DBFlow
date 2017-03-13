package com.raizlabs.android.dbflow.structure.caching

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelCacheField
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class, cachingEnabled = true)
class CacheableModel3 : BaseModel() {

    @Column
    @PrimaryKey
    var cache_id: String? = null

    @Column
    var number: Int = 0

    companion object {

        @JvmField
        @ModelCacheField
        val modelCache: ModelCache<CacheableModel3, *> = SimpleMapCache()
    }

}
