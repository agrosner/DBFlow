package com.raizlabs.android.dbflow.processor.writer;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface FlowWriter {

    public void write(JavaWriter javaWriter) throws IOException;
}
