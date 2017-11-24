package com.raizlabs.android.dbflow.models.issue;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.OneToManyMethod;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

import static com.raizlabs.android.dbflow.sql.language.SQLite.select;

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
