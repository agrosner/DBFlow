package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

/**
 * Description:
 */
@Table(database = KotlinTestDatabase::class)
data class Car(@PrimaryKey var id: Int = 0, @Column var name: String? = null) {
}