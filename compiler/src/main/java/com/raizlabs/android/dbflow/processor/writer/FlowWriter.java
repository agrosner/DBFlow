package com.raizlabs.android.dbflow.processor.writer;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Description: Base interface for writing something to the {@link com.squareup.javawriter.JavaWriter}
 */
public interface FlowWriter {

    /**
     * Writes to the {@link com.squareup.javawriter.JavaWriter}
     *
     * @param javaWriter Wrapper around writing files in annotation processing.
     * @throws IOException
     */
    public void write(JavaWriter javaWriter) throws IOException;
}
