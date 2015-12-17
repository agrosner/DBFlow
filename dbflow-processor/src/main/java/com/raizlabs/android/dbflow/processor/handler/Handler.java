package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;

/**
 * Description: The main base-level handler for performing some action when the
 * {@link javax.annotation.processing.Processor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)} is called.
 */
public interface Handler {

    /**
     * Called when the process of the {@link javax.annotation.processing.Processor} is called
     *
     * @param processorManager The manager that holds processing information
     * @param roundEnvironment The round environment
     */
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment);
}
