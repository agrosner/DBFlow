package com.raizlabs.dbflow5.structure

/**
 * Specifies the Action that was taken when data changes
 */
enum class ChangeAction {

    INSERT,

    UPDATE,

    DELETE,

    /**
     * The model was changed. used in prior to Android JellyBean and in generic change
     * actions like [com.raizlabs.dbflow5.query.StringQuery]
     */
    CHANGE
}