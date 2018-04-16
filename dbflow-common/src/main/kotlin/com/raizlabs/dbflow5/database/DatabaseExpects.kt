package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.config.DBFlowDatabase

expect open class PlatformOpenHelper(db: DBFlowDatabase, callback: DatabaseCallback?) : OpenHelper