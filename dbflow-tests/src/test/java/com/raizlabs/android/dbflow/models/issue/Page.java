package com.raizlabs.android.dbflow.models.issue;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class Page extends BaseModel {

    @PrimaryKey
    @Column
    String id;

    @PrimaryKey
    String owningIssueId;

    @ForeignKey(stubbedRelationship = true)
    SubIssue subIssue;
}
