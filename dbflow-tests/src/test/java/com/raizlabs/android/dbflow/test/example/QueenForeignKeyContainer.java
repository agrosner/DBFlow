package com.raizlabs.android.dbflow.test.example;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;

import java.util.Map;

/**
 * Description:
 */
public class QueenForeignKeyContainer extends ForeignKeyContainer<Queen> {

    public QueenForeignKeyContainer() {
        super(Queen.class);
    }

    public QueenForeignKeyContainer(Map<String, Object> data) {
        super(Queen.class, data);
    }

    public QueenForeignKeyContainer(@NonNull ModelContainer<Queen, ?> existingContainer) {
        super(existingContainer);
    }

    public QueenForeignKeyContainer(Class<Queen> queenClass) {
        super(queenClass);
    }
}
