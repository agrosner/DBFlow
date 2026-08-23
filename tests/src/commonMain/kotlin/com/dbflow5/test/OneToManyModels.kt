package com.dbflow5.test

import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
@OneToMany(
    childTable = OneToManyBaseModel::class,
)
data class OneToManyModel(@PrimaryKey val name: String)

@Table
data class OneToManyBaseModel(
    @PrimaryKey val id: Int,
    @ForeignKey(tableClass = OneToManyModel::class) val parentName: String?,
)
