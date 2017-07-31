package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.sql.Query
import org.junit.Assert.assertEquals


fun assertEquals(string: String, query: Query) = assertEquals(string, query.query.trim())