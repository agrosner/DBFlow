package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.OneToMany
import com.raizlabs.dbflow5.annotation.OneToManyMethod
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.BaseModel
import com.raizlabs.dbflow5.structure.oneToMany

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