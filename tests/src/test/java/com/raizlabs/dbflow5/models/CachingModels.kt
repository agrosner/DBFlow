package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.ForeignKey
import com.raizlabs.dbflow5.annotation.MultiCacheField
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.query.cache.IMultiKeyCacheConverter

@Table(database = TestDatabase::class, cachingEnabled = true)
class SimpleCacheObject(@PrimaryKey var id: String = "")

@Table(database = TestDatabase::class, cachingEnabled = true)
class Coordinate(@PrimaryKey var latitude: Double = 0.0,
                 @PrimaryKey var longitude: Double = 0.0,
                 @ForeignKey var path: Path? = null) {

    companion object {
        @JvmField
        @MultiCacheField
        val cacheConverter = object : IMultiKeyCacheConverter<String> {
            override fun getCachingKey(values: Array<Any>) = "${values[0]},${values[1]}"
        }
    }
}

@Table(database = TestDatabase::class)
class Path(@PrimaryKey var id: String = "",
           @Column var name: String = "")