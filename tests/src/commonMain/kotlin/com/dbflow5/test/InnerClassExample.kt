package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Example ensuring static inner classes work.
 */
class Outer {

    @Table
    class Inner(@PrimaryKey var id: Int = 0)
}
