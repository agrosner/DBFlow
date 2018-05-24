package com.dbflow5

import com.dbflow5.annotation.Database
import com.dbflow5.config.DBFlowDatabase

@Database(version = 1)
abstract class AppDatabase : DBFlowDatabase()