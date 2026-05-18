package com.zhousl.aether

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Phase 6 — visual regression baseline. Captures the Compose tree as a
 * bitmap and compares against a per-device baseline.
 *
 * Behavior:
 *  - First run (or UPDATE_BASELINE=1): writes the captured PNG to the device
 *    under app/external-files/screenshots/baseline/&lt;DEVICE_FP&gt;/welcome.png.
 *    The CI / local script pulls this back via extract-reports.sh.
 *  - Subsequent runs: pixel-compares with up to 2% pixel difference allowed.
 *
 * 方案 A (Appium): Phase 6 of the Appium POC implements an analogous flow
 * with @wdio/visual-service. The Native-frameworks variant is more direct —
 * Compose's `captureToImage()` produces a Bitmap with no protocol hop.
 *
 * Note: per-device baseline is needed because rendering varies with display
 * density / OEM theming. CI emulator uses a stable baseline; real devices
 * fall back to UPDATE_BASELINE=1 on first contact.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotRegressionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun welcomeScreenMatchesBaseline() {
        // Wait for onboarding step 1 to be visible.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            listOf("Welcome to Aether", "欢迎使用 Aether").any {
                composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
            }
        }
        composeRule.waitForIdle()

        val image = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val deviceFp = ctx.deviceFingerprint()

        val baselineDir = File(
            ctx.getExternalFilesDir(null) ?: ctx.filesDir,
            "screenshots/baseline/$deviceFp",
        ).apply { mkdirs() }
        val baselineFile = File(baselineDir, "welcome.png")
        val updateMode = !baselineFile.exists() || isUpdateBaselineRequested()

        if (updateMode) {
            FileOutputStream(baselineFile).use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            // Mark the run so the harness can tell baseline vs verification.
            val markerFile = File(baselineDir, "welcome.baseline-written")
            markerFile.writeText("device_fp=$deviceFp\nbytes=${baselineFile.length()}\n")
            return
        }

        val baseline = BitmapFactory.decodeFile(baselineFile.absolutePath)
            ?: error("Couldn't decode baseline at ${baselineFile.absolutePath}")
        val ratio = pixelDiffRatio(baseline, image)
        assertTrue(
            "Pixel diff vs baseline = ${"%.4f".format(ratio)} (>= 0.02 threshold). " +
                "Re-run with UPDATE_BASELINE=1 if the change is intentional.",
            ratio < 0.02f,
        )
    }

    private fun isUpdateBaselineRequested(): Boolean {
        val args = InstrumentationRegistry.getArguments()
        return args.getString("UPDATE_BASELINE") == "1" ||
            System.getenv("UPDATE_BASELINE") == "1"
    }

    private fun android.content.Context.deviceFingerprint(): String {
        // Mirrors scripts/run-on-device.sh DEVICE_FP derivation.
        val release = android.os.Build.VERSION.RELEASE
        val model = android.os.Build.MODEL
        return "android-$release-$model"
            .lowercase()
            .replace(Regex("[^a-z0-9._-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
    }

    private fun pixelDiffRatio(a: Bitmap, b: Bitmap): Float {
        if (a.width != b.width || a.height != b.height) return 1f
        val w = a.width
        val h = a.height
        val pa = IntArray(w * h)
        val pb = IntArray(w * h)
        a.getPixels(pa, 0, w, 0, 0, w, h)
        b.getPixels(pb, 0, w, 0, 0, w, h)
        var diffs = 0L
        for (i in pa.indices) {
            if (pa[i] != pb[i]) diffs++
        }
        return diffs.toFloat() / pa.size
    }
}
