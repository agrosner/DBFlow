package com.raizlabs.dbflow5.models.ants;

import com.raizlabs.dbflow5.TestDatabase;
import com.raizlabs.dbflow5.annotation.ForeignKey;
import com.raizlabs.dbflow5.annotation.ForeignKeyReference;
import com.raizlabs.dbflow5.annotation.PrimaryKey;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.structure.BaseModel;

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
