package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter

@Table(database = TestDatabase::class, cachingEnabled = true)
class SimpleCacheObject(@PrimaryKey var id: String = "")

@Table(database = TestDatabase::class, cachingEnabled = true)
class Coordinate(@PrimaryKey var latitude: Double = 0.0,
                 @PrimaryKey var longitude: Double = 0.0,
                 @ForeignKey var path: Path? = null) {

    companion object {
        @JvmField
        @MultiCacheField
        val cacheConverter = IMultiKeyCacheConverter { values -> "${values[0]},${values[1]}" }
    }
}

@Table(database = TestDatabase::class)
class Path(@PrimaryKey var id: String = "",
           @Column var name: String = "")