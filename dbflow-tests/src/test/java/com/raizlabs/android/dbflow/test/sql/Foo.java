package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.List;

@Table(database = TestDatabase.class)
public class Foo extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    String name;

    List<Bar> barList;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "barList")
    public List<Bar> getAllBars() {
        if (barList == null || barList.isEmpty()) {
            barList = SQLite.select()
                    .from(Bar.class)
                    .where(Bar_Table.fooForeignKeyContainer_id.eq(id))
                    .queryList();
        }
        return barList;
    }
}