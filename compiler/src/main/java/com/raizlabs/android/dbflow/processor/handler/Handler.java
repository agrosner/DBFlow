package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface Handler {

    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment);
}
