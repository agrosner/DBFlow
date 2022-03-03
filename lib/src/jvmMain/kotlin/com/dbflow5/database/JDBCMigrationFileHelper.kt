package com.dbflow5.database

import java.io.File
import java.io.InputStream

class JDBCMigrationFileHelper : JavaMigrationFileHelper {
    override fun getListFiles(dbMigrationPath: String): List<String> =
        JDBCMigrationFileHelper::class.java.classLoader?.getResource(dbMigrationPath)?.let { resource ->
            return File(resource.toURI()).listFiles()
                ?.map { it.name } ?: listOf()
        } ?: listOf()

    override fun openFileStream(fileName: String): InputStream {
        return JavaMigrationFileHelper::class.java.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalStateException("Cannot open file $fileName due to class loader issues.")
    }
}
