package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.rx.structure.BaseRXModel


@Table(database = TestDatabase::class, allFields = true)
class SimpleRXModel(@PrimaryKey var id: String = "") : BaseRXModel()