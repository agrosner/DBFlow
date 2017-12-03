package com.raizlabs.dbflow5.models.java;

import com.raizlabs.dbflow5.TestDatabase;
import com.raizlabs.dbflow5.annotation.Column;
import com.raizlabs.dbflow5.annotation.ModelView;
import com.raizlabs.dbflow5.annotation.ModelViewQuery;
import com.raizlabs.dbflow5.database.DatabaseWrapper;
import com.raizlabs.dbflow5.models.Author_Table;
import com.raizlabs.dbflow5.query.SQLite;
import com.raizlabs.dbflow5.sql.Query;

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
