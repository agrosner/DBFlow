package com.grosner.processor.writer;

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
