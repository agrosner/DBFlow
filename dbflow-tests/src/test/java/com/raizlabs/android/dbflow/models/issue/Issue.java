package com.raizlabs.android.dbflow.models.issue;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.OneToManyMethod;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Description:
 */

@Table(database = TestDatabase.class)
public class Issue extends BaseModel {

    @PrimaryKey
    String id;

    List<SubIssue> subIssueList;

    @OneToMany(oneToManyMethods = {OneToManyMethod.SAVE, OneToManyMethod.DELETE}, variableName = "subIssueList")
    public List<SubIssue> getDbSubIssueList() {
        if (subIssueList == null || subIssueList.isEmpty()) {
            subIssueList = SQLite.INSTANCE.select()
                    .from(SubIssue.class)
                    .where(SubIssue_Table.owningIssueId.eq(id))
                    .queryList();
        }
        return subIssueList;
    }
}
