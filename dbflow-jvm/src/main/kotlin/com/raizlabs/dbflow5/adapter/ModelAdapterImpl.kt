package com.raizlabs.dbflow5.adapter

import com.raizlabs.dbflow5.database.DBFlowDatabase

actual abstract class ModelAdapter<T : Any> actual constructor(database: DBFlowDatabase) : InternalModelAdapter<T>(database)
