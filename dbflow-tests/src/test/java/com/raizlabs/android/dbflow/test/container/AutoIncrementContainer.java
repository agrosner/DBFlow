package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class, useBooleanGetterSetters = true)
public class AutoIncrementContainer extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    long a_id;

    @Column
    private boolean aBoolean;

    @Column
    private boolean isEnabled;

    public boolean isABoolean() {
        return aBoolean;
    }

    public void setABoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
