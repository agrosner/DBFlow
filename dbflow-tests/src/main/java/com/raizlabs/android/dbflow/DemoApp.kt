package com.raizlabs.android.dbflow

import android.app.Application
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sqlcipher.CipherDatabase
import com.raizlabs.android.dbflow.sqlcipher.SQLCipherOpenHelperImpl

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FlowManager.init(FlowConfig.Builder(this)
            .addDatabaseConfig(DatabaseConfig.Builder(CipherDatabase::class.java)
                .transactionManagerCreator(::ImmediateTransactionManager)
                .openHelper(::SQLCipherOpenHelperImpl)
                .build()).build())
    }
}