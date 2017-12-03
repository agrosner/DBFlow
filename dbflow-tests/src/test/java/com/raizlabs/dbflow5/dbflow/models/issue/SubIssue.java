package com.raizlabs.dbflow5.dbflow.models.issue;

import com.raizlabs.dbflow5.dbflow.TestDatabase;
import com.raizlabs.dbflow5.annotation.OneToMany;
import com.raizlabs.dbflow5.annotation.OneToManyMethod;
import com.raizlabs.dbflow5.annotation.PrimaryKey;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.database.DatabaseWrapper;
import com.raizlabs.dbflow5.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

import static com.raizlabs.dbflow5.query.SQLite.select;

/**
 * Description:
 */

@Table(database = TestDatabase.class)
public class SubIssue extends BaseModel {

    @PrimaryKey
    String id;

    @PrimaryKey
    String owningIssueId;

    List<Page> pageList;

    @OneToMany(oneToManyMethods = {OneToManyMethod.SAVE, OneToManyMethod.DELETE}, variableName = "pageList")
    public List<Page> getDbPageList(DatabaseWrapper databaseWrapper) {
        if (pageList == null) {
            pageList = new ArrayList<>();
        }
        if (pageList.isEmpty()) {
            pageList = select(databaseWrapper)
                    .from(Page.class)
                    .where(Page_Table.owningIssueId.eq(owningIssueId), Page_Table.subIssue_id.eq(id))
                    .queryList();
        }
        return pageList;
    }
}
