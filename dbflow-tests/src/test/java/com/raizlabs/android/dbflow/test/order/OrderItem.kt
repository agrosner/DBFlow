package com.raizlabs.android.dbflow.test.order

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

@Table(name = "order_items", database = TestDatabase::class)
data class OrderItem(@PrimaryKey
                     @Column(name = "id")
                     var id: Int = 0,

                     @ForeignKey(saveForeignKeyModel = false)
                     var product: TestModel1? = null) : BaseModel()