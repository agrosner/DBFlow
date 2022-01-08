package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
class SimpleCacheObject(@PrimaryKey var id: String = "")

@Table
class Coordinate(
    @PrimaryKey var latitude: Double = 0.0,
    @PrimaryKey var longitude: Double = 0.0,
    @ForeignKey var path: Path? = null
)

@Table
class Path(
    @PrimaryKey var id: String = "",
    @Column var name: String = ""
)