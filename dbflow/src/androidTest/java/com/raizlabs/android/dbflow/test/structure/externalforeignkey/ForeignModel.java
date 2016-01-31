package com.raizlabs.android.dbflow.test.structure.externalforeignkey;

import com.raizlabs.android.dbflow.annotation.ExternalForeignKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.container.ExternalForeignKeyContainer;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

@Table(database=TestDatabase1.class)
public class ForeignModel extends TestModel1 {
    @ExternalForeignKey
    ExternalForeignKeyContainer<ForeignParentModel> foreignParentModel;

    public void setForeignParentModel(ForeignParentModel foreignParentModel) {
        this.foreignParentModel = FlowManager.getContainerAdapter(ForeignParentModel.class).toExternalForeignKeyContainer(foreignParentModel);
    }
}
