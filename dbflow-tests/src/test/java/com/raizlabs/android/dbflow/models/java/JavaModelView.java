package com.raizlabs.android.dbflow.models.java;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.models.Author_Table;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

@ModelView(database = TestDatabase.class)
public class JavaModelView {

    @ModelViewQuery
    public static Query getQuery(DatabaseWrapper databaseWrapper) {
        return SQLite.select(databaseWrapper, Author_Table.first_name.as("firstName"), Author_Table.id.as("id"));
    }

    @Column
    String id;

    @Column
    Integer firstName;

}
