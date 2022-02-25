package com.dbflow5.models.java;

import com.dbflow5.adapter.ModelAdapter;
import com.dbflow5.annotation.Column;
import com.dbflow5.annotation.ModelView;
import com.dbflow5.annotation.ModelViewQuery;
import com.dbflow5.models.Author;
import com.dbflow5.models.Author_Table;
import com.dbflow5.query.SelectKt;
import com.dbflow5.sql.Query;

@ModelView
public class JavaModelView {

    @ModelViewQuery
    public static Query getQuery(ModelAdapter<Author> authorAdapter) {
        return SelectKt.select(authorAdapter,
            Author_Table.first_name.as("firstName"),
            Author_Table.id.as("id"));
    }

    @Column
    String id;

    @Column
    Integer firstName;

}
