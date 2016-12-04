package com.raizlabs.android.dbflow.test.sqlcipher;

import android.content.Context;
import android.os.Build;

import com.raizlabs.android.dbflow.test.BuildConfig;
import com.raizlabs.android.dbflow.test.ShadowContentResolver2;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Description:
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP, shadows = {ShadowContentResolver2.class})
public abstract class CipherTestCase {

    @Rule
    public CipherTestRule dbFlowTestRule = CipherTestRule.create();

    protected Context getContext() {
        return RuntimeEnvironment.application;
    }
}