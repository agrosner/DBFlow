package com.dbflow5.data

/**
 * Description: Provides a way to support blob format data.
 */
class Blob(
    /**
     * Sets the underlying blob data.
     *
     * @param blob The set of bytes to use.
     */
    val blob: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Blob) return false

        if (!blob.contentEquals(other.blob)) return false

        return true
    }

    override fun hashCode(): Int {
        return blob.contentHashCode()
    }
}

