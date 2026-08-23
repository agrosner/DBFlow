package com.dbflow5.models.java;

import com.dbflow5.annotation.Column;
import com.dbflow5.annotation.ModelView;

@ModelView(query = "SELECT `first_name` AS `firstName`, `id` AS `id` FROM `Author`")
public class JavaModelView {

    @Column
    String id;

    @Column
    Integer firstName;

}
