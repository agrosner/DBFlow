package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.structure.BaseModel.Action;

/**
 * Description: Provides {@link Action} for SQL constructs.
 */
public interface Actionable {

    Action getPrimaryAction();
}
