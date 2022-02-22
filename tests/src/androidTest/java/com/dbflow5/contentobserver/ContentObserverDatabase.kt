package com.dbflow5.contentobserver

import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.config.DBFlowDatabase

@Database(
    version = 1,
    tables = [
        User::class,
        com.dbflow5.User::class
    ],
)
abstract class ContentObserverDatabase : DBFlowDatabase() {

    abstract val userAdapter: ModelAdapter<User>
    abstract val user2Adapter: ModelAdapter<com.dbflow5.User>
}
