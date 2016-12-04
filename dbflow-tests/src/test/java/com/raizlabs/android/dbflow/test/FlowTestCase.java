package com.raizlabs.android.dbflow.test;

import android.content.Context;
import android.os.Build;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Description:
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public abstract class FlowTestCase {

    @Rule
    public DBFlowTestRule dbFlowTestRule = DBFlowTestRule.create();

    protected Context getContext() {
        return RuntimeEnvironment.application;
    }
}
