package com.dbflow5.test

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.config.DBFlowDatabase

@Database(
    tables = [
        SimpleModel::class,
        CaseModel::class,
        TwoColumnModel::class,
        EnumTypeConverterModel::class,
    ],
    version = 1,
)
abstract class TestDatabase : DBFlowDatabase() {

    abstract val simpleModelAdapter: ModelAdapter<SimpleModel>
}
