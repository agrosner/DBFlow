package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table

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
