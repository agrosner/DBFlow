package com.raizlabs.android.dbflow.test.structure.onetomany;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ListColumn;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.List;

/**
 * Description: Reference table for the {@link ListColumn} relationships.
 *
 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase.class)
public class ListTable {

    @PrimaryKey(autoincrement = true)
    int id;

    @ListColumn
    List<Child> children;

    @Table(database = TestDatabase.class)
    public static class Child {

        @PrimaryKey
        int id;

        @Column
        String reference;

        @ForeignKey(stubbedRelationship = true)
        ListTable parent;
    }
}
