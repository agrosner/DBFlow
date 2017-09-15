package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

@Table(database = AppDatabase::class, name = "User2")
class User {

    @PrimaryKey
    var id: Int = 0

    @Column
    var firstName: String? = null

    @Column
    var lastName: String? = null

    @Column
    var email: String? = null
}
