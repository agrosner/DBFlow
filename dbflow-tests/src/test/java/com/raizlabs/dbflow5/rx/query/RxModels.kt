package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.rx.structure.BaseRXModel


@Table(database = TestDatabase::class, allFields = true)
class SimpleRXModel(@PrimaryKey var id: String = "") : BaseRXModel()

@Table(database = TestDatabase::class)
class SimpleRXModel2(@PrimaryKey var id: String = "") : com.raizlabs.dbflow5.rx2.structure.BaseRXModel()