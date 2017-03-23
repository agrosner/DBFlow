package com.raizlabs.android.dbflow

import android.app.Application
import android.content.Context
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sqlcipher.CipherDatabase
import com.raizlabs.android.dbflow.sqlcipher.SQLCipherOpenHelperImpl

class DemoApp : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        FlowManager.init(FlowConfig.Builder(this)
            .addDatabaseConfig(DatabaseConfig.Builder(CipherDatabase::class.java)
                .transactionManagerCreator(::ImmediateTransactionManager)
                .openHelper(::SQLCipherOpenHelperImpl)
                .build()).build())
    }
}