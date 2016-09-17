package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class, cachingEnabled = true, useBooleanGetterSetters = true)
public class AIContainerForeign extends AutoIncrementContainer {


    @Column
    @ForeignKey(references = {
            @ForeignKeyReference(columnType = long.class, columnName = "ai_container_id", foreignKeyColumnName = "id")})
    AutoIncrementContainer foreignModel;

    @Column
    @ForeignKey(references = {
            @ForeignKeyReference(columnType = long.class, columnName = "ai_container_id_container",
                    foreignKeyColumnName = "id")})
    AutoIncrementContainer container;

    public void setContainer(AutoIncrementContainer autoIncrementContainer) {
        container = autoIncrementContainer;
    }
}
