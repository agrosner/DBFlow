package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

@Table(database = AppDatabase.class, name = "User2")
public class User {

    @PrimaryKey
    int id;

    @Column
    String firstName;

    @Column
    String lastName;

    @Column
    String email;
}
