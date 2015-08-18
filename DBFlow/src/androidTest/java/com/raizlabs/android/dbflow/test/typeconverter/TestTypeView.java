package com.raizlabs.android.dbflow.test.typeconverter;

import android.location.Location;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

/**
 * Author: kzsolti
 */
@ModelView(databaseName = TestDatabase.NAME, query = "SELECT * FROM TestType")
public class TestTypeView extends BaseModelView<TestType> {

	@Column
	public String name;

	@Column
	boolean nativeBoolean;

	@Column
	Boolean aBoolean;

	@Column
	Calendar calendar;

	@Column
	Date date;

	@Column
	java.sql.Date sqlDate;

	@Column
	JSONObject json;

	@Column
	Location location;
}
