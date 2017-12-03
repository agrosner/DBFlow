package com.raizlabs.dbflow5.models.issue;

import com.raizlabs.dbflow5.TestDatabase;
import com.raizlabs.dbflow5.annotation.Column;
import com.raizlabs.dbflow5.annotation.ForeignKey;
import com.raizlabs.dbflow5.annotation.PrimaryKey;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.structure.BaseModel;

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
