package com.raizlabs.android.dbflow.test.NonTypical

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:

 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase::class)
class nonTypicalModelName {

    @PrimaryKey
    var id: Long = 0
}
