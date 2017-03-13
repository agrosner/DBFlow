package com.raizlabs.android.dbflow.sql.fast

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class NonFastModel : FastModel()
