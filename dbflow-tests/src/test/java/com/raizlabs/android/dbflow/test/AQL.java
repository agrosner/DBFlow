package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.sql.BoxedModel;

import java.util.Date;

@Table(database = TestDatabase.class, name = AQL.ENDPOINT)
public class AQL extends BaseModel {

    public static final String ENDPOINT = "AQL";

    public interface Columns {
        String AQL_ID = "aql_id";
        String AQL_NAME = "aql_name";
        String AQL_SERVER_ID = "aql_server_id";
        String AQL_TIMESTAMP = "aql_timestamp";
    }

    @Column(name = Columns.AQL_ID)
    @PrimaryKey(autoincrement = true)
    private Long aql_id;

    @Column(name = Columns.AQL_NAME)
    private String aql_name;

    @Column(name = Columns.AQL_SERVER_ID)
    private Long server_id;

    @Column(name = Columns.AQL_TIMESTAMP)
    private Date timestamp;

    @ContainerKey("boxedLodo")
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id1", columnType = long.class, foreignKeyColumnName = "id", referencedFieldIsPackagePrivate = true),
            @ForeignKeyReference(columnName = "id2", columnType = String.class, foreignKeyColumnName = "name", referencedFieldIsPackagePrivate = true)})
    private BoxedModel boxedModel;

    public Long getAql_id() {
        return aql_id;
    }

    public void setAql_id(Long aql_id) {
        this.aql_id = aql_id;
    }

    public String getAql_name() {
        return aql_name;
    }

    public void setAql_name(String aql_name) {
        this.aql_name = aql_name;
    }

    public Long getServer_id() {
        return server_id;
    }

    public void setServer_id(Long server_id) {
        this.server_id = server_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BoxedModel getBoxedModel() {
        return boxedModel;
    }

    public void setBoxedModel(BoxedModel boxedModel) {
        this.boxedModel = boxedModel;
    }
}
