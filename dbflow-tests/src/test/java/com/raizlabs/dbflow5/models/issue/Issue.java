package com.raizlabs.dbflow5.models.issue;

import com.raizlabs.dbflow5.TestDatabase;
import com.raizlabs.dbflow5.annotation.OneToMany;
import com.raizlabs.dbflow5.annotation.OneToManyMethod;
import com.raizlabs.dbflow5.annotation.PrimaryKey;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.database.DatabaseWrapper;
import com.raizlabs.dbflow5.structure.BaseModel;

import java.util.List;

import static com.raizlabs.dbflow5.query.SQLite.select;

/**
 * Description:
 */

@Table(database = TestDatabase.class)
public class Issue extends BaseModel {

    @PrimaryKey
    String id;

    List<SubIssue> subIssueList;

    @OneToMany(oneToManyMethods = {OneToManyMethod.SAVE, OneToManyMethod.DELETE}, variableName = "subIssueList")
    public List<SubIssue> getDbSubIssueList(DatabaseWrapper databaseWrapper) {
        if (subIssueList == null || subIssueList.isEmpty()) {
            subIssueList = select()
                    .from(SubIssue.class)
                    .where(SubIssue_Table.owningIssueId.eq(id))
                    .queryList(databaseWrapper);
        }
        return subIssueList;
    }
}
