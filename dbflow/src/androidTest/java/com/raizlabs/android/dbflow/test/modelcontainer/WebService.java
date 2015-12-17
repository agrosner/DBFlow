package com.raizlabs.android.dbflow.test.modelcontainer;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(name = "web_service", database = TestDatabase.class)
@ModelContainer
public class WebService extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long pid;

    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "pass_type_pid",
                    columnType = long.class,
                    foreignKeyColumnName = "pid")},
            saveForeignKeyModel = false)
    ForeignKeyContainer<PassType> passType;

    @Column(name = "service_url") String serviceUrl;

    public void associatePassType(PassType passType) {
        this.passType = FlowManager.getContainerAdapter(PassType.class).toForeignKeyContainer(passType);
    }
}