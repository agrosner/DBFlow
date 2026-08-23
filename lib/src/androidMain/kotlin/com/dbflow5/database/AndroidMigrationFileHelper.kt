package com.dbflow5.database

import android.content.Context
import java.io.InputStream

/**
 * Description: Implements [MigrationFileHelper] for Android targets.
 */
class AndroidMigrationFileHelper(private val context: Context) : JavaMigrationFileHelper {
    override fun getListFiles(dbMigrationPath: String): List<String> =
        context.assets.list(dbMigrationPath)?.toList() ?: listOf()

    override fun openFileStream(fileName: String): InputStream = context.assets.open(fileName)
}
