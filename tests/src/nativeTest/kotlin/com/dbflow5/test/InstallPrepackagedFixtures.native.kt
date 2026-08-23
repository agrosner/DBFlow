package com.dbflow5.test

import co.touchlab.sqliter.DatabaseFileContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import okio.FileSystem
import okio.Path.Companion.toPath

actual fun installPrepackagedFixtures() {
    writeFixture("prepackaged.db", PREPACKAGED_DB_FIXTURE)
    writeFixture("prepackaged_2.db", PREPACKAGED_2_DB_FIXTURE)
}

@OptIn(ExperimentalEncodingApi::class)
private fun writeFixture(name: String, encoded: String) {
    val dest = DatabaseFileContext.databasePath(name, null).toPath()
    val fileSystem = FileSystem.SYSTEM
    if (fileSystem.exists(dest) && (fileSystem.metadata(dest).size ?: 0L) > 0L) {
        return
    }
    dest.parent?.let { fileSystem.createDirectories(it) }
    fileSystem.write(dest) {
        write(Base64.decode(encoded.filterNot(Char::isWhitespace)))
    }
}
