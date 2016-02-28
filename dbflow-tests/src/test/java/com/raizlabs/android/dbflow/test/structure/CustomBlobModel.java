package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@QueryModel(database = TestDatabase.class)
public class CustomBlobModel extends BaseQueryModel {

    public static class MyBlob {
        private final byte[] blob;

        public MyBlob(byte[] blob) {
            this.blob = blob;
        }
    }

    @com.raizlabs.android.dbflow.annotation.TypeConverter
    public static class MyTypeConverter extends TypeConverter<Blob, MyBlob> {

        @Override
        public Blob getDBValue(MyBlob model) {
            return new Blob(model.blob);
        }

        @Override
        public MyBlob getModelValue(Blob data) {
            return new MyBlob(data.getBlob());
        }
    }

    @Column
    MyBlob myBlob;
}