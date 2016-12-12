package com.raizlabs.android.dbflow.test.sql.fast

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class NonFastModel : FastModel()
