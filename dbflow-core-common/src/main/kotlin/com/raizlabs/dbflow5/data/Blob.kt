package com.raizlabs.dbflow5.data

import com.raizlabs.dbflow5.JvmOverloads

/**
 * Description: Provides a way to support blob format data.
 */
class Blob @JvmOverloads constructor(
    /**
     * Sets the underlying blob data.
     *
     * @param blob The set of bytes to use.
     */
    var blob: ByteArray? = null)
