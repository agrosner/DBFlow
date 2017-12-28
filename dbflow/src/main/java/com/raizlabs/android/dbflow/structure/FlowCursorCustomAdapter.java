package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.structure.database.FlowCursor;

public interface FlowCursorCustomAdapter<AnyType> {

    AnyType loadFromCursor(FlowCursor flowCursor);
}
