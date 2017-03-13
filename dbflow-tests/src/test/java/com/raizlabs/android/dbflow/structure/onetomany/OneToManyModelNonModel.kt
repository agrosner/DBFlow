package com.raizlabs.android.dbflow.structure.onetomany

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:

 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase::class)
class OneToManyModelNonModel {

    @PrimaryKey
    var id: Int = 0

    @PrimaryKey
    var name: String? = null
}
