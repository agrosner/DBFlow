package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table(database = TestDatabase::class)
@OneToManyRelation(
    childTable = OneToManyBaseModel::class,
)
data class OneToManyModel(@PrimaryKey var name: String = "")

@Table(database = TestDatabase::class)
data class OneToManyBaseModel(
    @PrimaryKey var id: Int = 0,
    @ForeignKey(tableClass = OneToManyModel::class) var parentName: String? = "",
)
