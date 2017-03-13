package com.raizlabs.android.dbflow.NonTypical

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:

 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase::class)
class nonTypicalModelName {

    @PrimaryKey
    var id: Long = 0
}
