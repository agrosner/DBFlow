package com.raizlabs.android.dbflow.transaction

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class, cachingEnabled = true)
class TrCacheableModel : TestModel1()
