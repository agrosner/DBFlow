package com.raizlabs.android.dbflow.structure

/**
 * Specifies the Action that was taken when data changes
 */
enum class ChangeAction {

    /**
     * Save called. Is paired with [UPDATE] or [INSERT] depending on action taken.
     */
    SAVE,

    INSERT,

    UPDATE,

    DELETE,

    /**
     * The model was changed. used in prior to [android.os.Build.VERSION_CODES.JELLY_BEAN_MR1]
     */
    CHANGE
}