package com.dbflow5.database

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

fun interface DatabaseWriter {
    @Throws(IOException::class)
    fun write(dbPath: File, existingDB: InputStream)
}

fun DatabaseWriter(): DatabaseWriter = DatabaseWriterImpl()

internal class DatabaseWriterImpl : DatabaseWriter {

    /**
     * Writes the [InputStream] of the existing db to the file specified.
     *
     * @param dbPath     The file to write to.
     * @param existingDB The existing database file's input stream¬
     * @throws IOException
     */
    override fun write(dbPath: File, existingDB: InputStream) {
        val output = FileOutputStream(dbPath)

        val buffer = ByteArray(1024)
        var length: Int = existingDB.read(buffer)
        while (length > 0) {
            output.write(buffer, 0, length)
            length = existingDB.read(buffer)
        }

        output.flush()
        output.close()
        existingDB.close()
    }
}