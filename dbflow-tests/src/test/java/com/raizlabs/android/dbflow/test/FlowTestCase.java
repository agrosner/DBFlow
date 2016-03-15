package com.raizlabs.android.dbflow.test;

import android.content.Context;
import android.os.Build;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Description:
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP, shadows = {ShadowContentResolver2.class})
public abstract class FlowTestCase {

    @Rule
    public DBFlowTestRule dbFlowTestRule = DBFlowTestRule.create();

    protected Context getContext() {
        return RuntimeEnvironment.application;
    }
}
