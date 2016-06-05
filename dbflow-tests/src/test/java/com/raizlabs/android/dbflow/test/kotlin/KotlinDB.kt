package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description:
 */
@Table(database = KotlinTestDatabase::class)
data class Car(@PrimaryKey var id: Int = 0, @Column var name: String? = null) : Model {

    override fun save() = FlowManager.getModelAdapter(javaClass).save(this)

    override fun delete() = FlowManager.getModelAdapter(javaClass).delete(this)

    override fun update() = FlowManager.getModelAdapter(javaClass).update(this)

    override fun insert() = FlowManager.getModelAdapter(javaClass).insert(this)

    override fun exists() = FlowManager.getModelAdapter(javaClass).exists(this)
}