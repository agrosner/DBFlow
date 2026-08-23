package com.dbflow5.test

import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual fun installPrepackagedFixtures() {
    writeFixture("prepackaged.db", PREPACKAGED_DB_FIXTURE)
    writeFixture("prepackaged_2.db", PREPACKAGED_2_DB_FIXTURE)
}

@OptIn(ExperimentalEncodingApi::class)
private fun writeFixture(name: String, encoded: String) {
    // JVM databases resolve relative to the working directory.
    val dest = File(name)
    if (dest.exists() && dest.length() > 0L) return
    dest.writeBytes(Base64.decode(encoded.filterNot(Char::isWhitespace)))
}
