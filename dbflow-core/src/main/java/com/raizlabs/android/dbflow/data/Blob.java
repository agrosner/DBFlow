package com.raizlabs.android.dbflow.data;

/**
 * Description: Provides a way to support blob format data.
 */
public class Blob {

    private byte[] blob;

    public Blob(byte[] blob) {
        this.blob = blob;
    }

    /**
     * Sets the underlying blob data.
     *
     * @param blob The set of bytes to use.
     */
    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    /**
     * @return The blob data.
     */
    public byte[] getBlob() {
        return blob;
    }
}
