package com.raizlabs.dbflow5.sqlcipher

import com.raizlabs.dbflow5.annotation.Database
import com.raizlabs.dbflow5.config.DBFlowDatabase

@Database(version = 1)
abstract class CipherDatabase : DBFlowDatabase()