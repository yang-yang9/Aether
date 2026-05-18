package com.zhousl.aether

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 1 placeholder so `:app:assembleDebugAndroidTest` produces a non-empty
 * test APK. P2-P6 will replace / supplement this with the real
 * Native / WebView / Bridge / CrossApp / Screenshot test classes.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @Test
    fun targetPackageIsAether() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.baimoqilin.aether", ctx.packageName)
    }
}
