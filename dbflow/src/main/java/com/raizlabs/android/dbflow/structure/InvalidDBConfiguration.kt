package com.raizlabs.android.dbflow.structure

/**
 * Description: Thrown when a DB is incorrectly configured.
 */
class InvalidDBConfiguration : RuntimeException {

    constructor() : super("No Databases were found. Did you create a Database Annotation placeholder class?")

    constructor(message: String) : super(message) {}
}
