package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelView(database = TestDatabase.class)
public class BlobTestView extends BaseModelView<BlobTestView.DataTable> {

    @ModelViewQuery
    public static final Query QUERY = new Select(DataTable_Table.data).from(DataTable.class);

    @Column
    public Blob data;


    @Table(database = TestDatabase.class)
    public static class DataTable extends BaseModel {

        @PrimaryKey(autoincrement = true)
        long id;

        @Column
        Blob data;
    }
}
