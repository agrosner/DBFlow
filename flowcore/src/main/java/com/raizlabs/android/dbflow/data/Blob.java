package com.raizlabs.android.dbflow.data;

/**
 * Description: Provides a way to support blob format data.
 */
public class Blob {

    private byte[] blob;

    public Blob(byte[] blob) {
        this.blob = blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public byte[] getBlob() {
        return blob;
    }
}
