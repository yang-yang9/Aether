package com.zhousl.aether

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 5 — UIAutomator cross-app capability. Demonstrates the white-box
 * stack's reach beyond the app's own process: read the current foreground
 * package, drive a system gesture (Home), re-launch the app via Intent and
 * confirm focus is back.
 *
 * 方案 A (Appium): can do this but requires Context switching out of NATIVE_APP.
 * 方案 C (Maestro): impossible — no foreground/launcher inspection, no
 *                   per-package selectors, no programmatic re-launch.
 * 方案 B (this):    UIAutomator + InstrumentationRegistry — first-class.
 */
@RunWith(AndroidJUnit4::class)
class CrossAppTest {
    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun homeAndRelaunchKeepsAetherForegroundable() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val aetherPkg = ctx.packageName

        launchAether(ctx, aetherPkg)
        device.wait(Until.hasObject(By.pkg(aetherPkg)), 5_000)
        assertEquals("Aether should be foreground after launch", aetherPkg, device.currentPackageName)

        device.pressHome()
        device.wait(Until.gone(By.pkg(aetherPkg)), 3_000)
        assertNotEquals("Home should leave Aether's package", aetherPkg, device.currentPackageName)

        launchAether(ctx, aetherPkg)
        device.wait(Until.hasObject(By.pkg(aetherPkg)), 5_000)
        assertEquals("Aether should be foreground after re-launch", aetherPkg, device.currentPackageName)
    }

    private fun launchAether(ctx: android.content.Context, packageName: String) {
        val intent = ctx.packageManager.getLaunchIntentForPackage(packageName)!!
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        ctx.startActivity(intent)
    }
}
