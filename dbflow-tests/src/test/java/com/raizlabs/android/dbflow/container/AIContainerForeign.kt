package com.raizlabs.android.dbflow.container

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, cachingEnabled = true)
class AIContainerForeign : AutoIncrementContainer() {

    @Column
    @ForeignKey(stubbedRelationship = true)
    var foreignModel: AutoIncrementContainer? = null

    @Column
    @ForeignKey(stubbedRelationship = true)
    var container: AutoIncrementContainer? = null

}
