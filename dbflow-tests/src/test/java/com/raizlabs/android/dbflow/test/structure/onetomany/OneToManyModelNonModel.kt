package com.raizlabs.android.dbflow.test.structure.onetomany

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:

 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase::class)
class OneToManyModelNonModel {

    @PrimaryKey
    var id: Int = 0

    @PrimaryKey
    var name: String
}
