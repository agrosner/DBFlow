package com.dbflow5.mpp

import kotlin.native.concurrent.ensureNeverFrozen

actual fun Any.ensureNeverFrozen() {
    this.ensureNeverFrozen()
}
