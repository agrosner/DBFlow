package com.grosner.dbflow.structure;

/**
 * Author: andrewgrosner
 * Description: Thrown when a DB is incorrectly configured.
 */
public class InvalidDBConfiguration extends RuntimeException {

    public InvalidDBConfiguration() {
        super("No Databases were found. Did you create a Database Annotation placeholder class?");
    }

    public InvalidDBConfiguration(String modelName) {
        super("The manager in a multidatabase setup did not include " + modelName + " in the DBConfiguration");
    }
}
