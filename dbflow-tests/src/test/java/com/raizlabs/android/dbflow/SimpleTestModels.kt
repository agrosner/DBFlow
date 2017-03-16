package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.annotation.Table

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class SimpleModel(@PrimaryKey var name: String? = "")

@QueryModel(database = TestDatabase::class)
class SimpleCustomModel(@Column var name: String? = "")

@Table(database = TestDatabase::class)
class TwoColumnModel(@PrimaryKey var name: String? = "", @Column var id: Int = 0)