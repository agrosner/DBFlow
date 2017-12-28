package com.raizlabs.android.dbflow.models.java;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.models.Author_Table;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.SQLite;

@ModelView(database = TestDatabase.class)
public class JavaModelView {

    @ModelViewQuery
    public static final Query QUERY = getQuery();

    @Column
    String id;

    @Column
    Integer firstName;

    private static Query getQuery() {
        return SQLite.select(Author_Table.first_name.as("firstName"), Author_Table.id.as("id"));
    }
}
