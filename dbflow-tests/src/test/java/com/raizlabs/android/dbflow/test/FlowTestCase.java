package com.raizlabs.android.dbflow.test;

import android.content.Context;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Description:
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config
public abstract class FlowTestCase {

    @Rule
    protected DBFlowTestRule dbFlowTestRule = DBFlowTestRule.create();

    protected Context getContext() {
        return RuntimeEnvironment.application;
    }
}
