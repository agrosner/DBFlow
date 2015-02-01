package com.raizlabs.android.dbflow.annotation;

/**
 * Description: Defines how a {@link com.raizlabs.android.dbflow.annotation.ForeignKeyReference} that is a model
 * is loading and saved with the model. This is to ensure optimal performance loading.
 */
public enum ForeignModelInteraction {

    /**
     * Default behavior. It will load and save the foreign Model.
     */
    LOAD_AUTO,

    /**
     * Will only load the model when the parent model is retrieved from the database.
     */
    LOAD_ONLY,

    /**
     * Saves the model only to the database. Will not load the foreign model and could lead to loss
     * of proper data unless you utilize a LoadFromCursorListener.
     */
    SAVE_ONLY,

    /**
     * Loading and saving will not be handled by the ModelAdapter, rather the class should
     * implement LoadFromCursorLister, SQLStatementListener, and ContentValuesListener manually.
     */
    NONE
}
