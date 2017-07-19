package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.oneToMany
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.structure.BaseModel

@Table(database = TestDatabase::class)
class OneToManyModel(@PrimaryKey var name: String? = null) {

    var orders: List<TwoColumnModel>? = null

    var models: List<OneToManyBaseModel>? = null

    @get:OneToMany(methods = arrayOf(OneToMany.Method.ALL))
    var simpleModels by oneToMany { select from OneToManyBaseModel::class }

    @OneToMany(methods = arrayOf(OneToMany.Method.ALL), isVariablePrivate = true,
        variableName = "orders", efficientMethods = false)
    fun getRelatedOrders(): List<TwoColumnModel> {
        var localOrders = orders
        if (localOrders == null) {
            localOrders = (select from TwoColumnModel::class where id.greaterThan(3))
                .queryList()
        }
        orders = localOrders
        return localOrders
    }

    @OneToMany(methods = arrayOf(OneToMany.Method.DELETE), isVariablePrivate = true,
        variableName = "models")
    fun getRelatedModels(): List<OneToManyBaseModel> {
        var localModels = models
        if (localModels == null) {
            localModels = (select from OneToManyBaseModel::class).queryList()
        }
        models = localModels
        return localModels
    }


}

@Table(database = TestDatabase::class)
class OneToManyBaseModel(@PrimaryKey var id: Int = 0) : BaseModel()