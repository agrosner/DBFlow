package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.rx.structure.BaseRXModel


@Table(database = TestDatabase::class, allFields = true)
class SimpleRXModel(@PrimaryKey var id: String = "") : BaseRXModel()

@Table(database = TestDatabase::class)
class SimpleRXModel2(@PrimaryKey var id: String = "") : com.raizlabs.android.dbflow.rx2.structure.BaseRXModel()