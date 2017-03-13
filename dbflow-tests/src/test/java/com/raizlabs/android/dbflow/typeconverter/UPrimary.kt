package com.raizlabs.android.dbflow.typeconverter

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

import java.util.UUID

/**
 * Description: Example of a class with type converted primary key used as a [ForeignKey].

 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase::class)
class UPrimary {

    @PrimaryKey
    var uuid: UUID? = null

}
