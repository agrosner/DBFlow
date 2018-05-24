package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table

/**
 * Example ensuring static inner classes work.
 */
class Outer {

    @Table(database = TestDatabase::class)
    class Inner(@PrimaryKey var id: Int = 0)
}