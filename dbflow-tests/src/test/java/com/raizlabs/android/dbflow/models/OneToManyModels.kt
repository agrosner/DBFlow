package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.OneToManyMethod
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.query.where
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.oneToMany

@Table(database = TestDatabase::class)
class OneToManyModel(@PrimaryKey var name: String? = null) {

    var orders: List<TwoColumnModel>? = null

    var models: List<OneToManyBaseModel>? = null

    @get:OneToMany(oneToManyMethods = arrayOf(OneToManyMethod.ALL))
    var simpleModels by oneToMany {
        databaseForTable(OneToManyBaseModel::class) {
            select from OneToManyBaseModel::class
        }
    }

    @OneToMany(oneToManyMethods = arrayOf(OneToManyMethod.ALL), isVariablePrivate = true,
            variableName = "orders", efficientMethods = false)
    fun getRelatedOrders(wrapper: DatabaseWrapper): List<TwoColumnModel> {
        var localOrders = orders
        if (localOrders == null) {
            localOrders = (wrapper.select from TwoColumnModel::class where id.greaterThan(3))
                    .queryList()
        }
        orders = localOrders
        return localOrders
    }

    @OneToMany(oneToManyMethods = arrayOf(OneToManyMethod.DELETE), isVariablePrivate = true,
            variableName = "models")
    fun getRelatedModels(wrapper: DatabaseWrapper): List<OneToManyBaseModel> {
        var localModels = models
        if (localModels == null) {
            localModels = (wrapper.select from OneToManyBaseModel::class).queryList()
        }
        models = localModels
        return localModels
    }


}

@Table(database = TestDatabase::class)
class OneToManyBaseModel(@PrimaryKey var id: Int = 0) : BaseModel()