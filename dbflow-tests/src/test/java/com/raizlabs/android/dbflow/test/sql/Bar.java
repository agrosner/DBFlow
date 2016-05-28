package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelContainer
@Table(database = TestDatabase.class)
public class Bar extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    ForeignKeyContainer<Foo> fooForeignKeyContainer;

    public void associateFoo(Foo foo) {
        fooForeignKeyContainer =
                new ForeignKeyContainer<>(FlowManager.getContainerAdapter(Foo.class)
                        .toForeignKeyContainer(foo));
    }
}