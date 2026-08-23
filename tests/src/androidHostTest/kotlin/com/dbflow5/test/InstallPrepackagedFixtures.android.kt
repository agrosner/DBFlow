package com.dbflow5.test

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.robolectric.RuntimeEnvironment

actual fun installPrepackagedFixtures() {
    writeFixture("prepackaged.db", PREPACKAGED_DB_FIXTURE)
    writeFixture("prepackaged_2.db", PREPACKAGED_2_DB_FIXTURE)
}

@OptIn(ExperimentalEncodingApi::class)
private fun writeFixture(name: String, encoded: String) {
    val dest = RuntimeEnvironment.getApplication().getDatabasePath(name)
    if (dest.exists() && dest.length() > 0L) return
    dest.parentFile?.mkdirs()
    dest.writeBytes(Base64.decode(encoded.filterNot(Char::isWhitespace)))
}
