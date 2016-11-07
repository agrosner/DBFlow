package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelView(database = TestDatabase.class, name = "v_view", priority = 3)
public class MyView extends BaseModelView {

    @ModelViewQuery
    public static final Query QUERY = SQLite.select().from(TestModel1.class);

    public enum TestEnum {YES, NO}

    @Column
    public TestEnum value;

    @Column
    public Boolean isSet;

    @Column(name = "is_up_next", getterName = "isUpNext", setterName = "setUpNext")
    private boolean mIsUpNext;

    @Column(name = "is_favorite", getterName = "isFavorite", setterName = "setFavorite")
    private boolean mIsFavorite;

    public boolean isUpNext() {
        return mIsUpNext;
    }

    public void setUpNext(boolean mIsUpNext) {
        this.mIsUpNext = mIsUpNext;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }
}
