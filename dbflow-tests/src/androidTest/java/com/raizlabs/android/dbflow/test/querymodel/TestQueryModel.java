package com.raizlabs.android.dbflow.test.querymodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@QueryModel(database = TestDatabase.class)
public class TestQueryModel extends BaseQueryModel {

    @Column
    String newName;

    @Column
    long average_salary;

    @Column
    String department;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestQueryModel that = (TestQueryModel) o;

        if (average_salary != that.average_salary) {
            return false;
        }
        if (newName != null ? !newName.equals(that.newName) : that.newName != null) {
            return false;
        }
        return !(department != null ? !department.equals(that.department) : that.department != null);

    }

    @Override
    public int hashCode() {
        int result = newName != null ? newName.hashCode() : 0;
        result = 31 * result + (int) (average_salary ^ (average_salary >>> 32));
        result = 31 * result + (department != null ? department.hashCode() : 0);
        return result;
    }
}
