package com.grosner.processor.handler;

import com.grosner.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface Handler {

    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment);
}
