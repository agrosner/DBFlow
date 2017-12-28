package com.raizlabs.android.dbflow.models.ants;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = TestDatabase.class)
public class Ants extends BaseModel {
    @PrimaryKey
    public String antId;

    @PrimaryKey
    @ForeignKey(tableClass = AntHill.class, references = {
            @ForeignKeyReference(columnName = "hillIdRef", foreignKeyColumnName = "hillId")
    })
    public String hillId;
}
