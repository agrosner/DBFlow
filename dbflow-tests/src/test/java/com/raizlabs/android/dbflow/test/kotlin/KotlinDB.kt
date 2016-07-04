package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.modelAdapter
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description:
 */
@Table(database = KotlinTestDatabase::class)
data class Car(@PrimaryKey var id: Int = 0, @Column var name: String? = null) : Model {

    override fun save() = modelAdapter<Car>().save(this)

    override fun delete() = modelAdapter<Car>().delete(this)

    override fun update() = modelAdapter<Car>().update(this)

    override fun insert() = modelAdapter<Car>().insert(this)

    override fun exists() = modelAdapter<Car>().exists(this)
}