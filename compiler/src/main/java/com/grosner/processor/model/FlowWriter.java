package com.grosner.processor.model;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface FlowWriter {

    /**
     * Returns the fully qualified class name.
     * @return
     */
    public String getFQCN();

    public void write(JavaWriter javaWriter) throws IOException;
}
