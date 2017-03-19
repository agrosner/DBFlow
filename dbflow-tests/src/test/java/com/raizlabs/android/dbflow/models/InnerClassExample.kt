package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

/**
 * Example ensuring static inner classes work.
 */
class Outer {

    @Table(database = TestDatabase::class)
    class Inner(@PrimaryKey var id: Int = 0)
}