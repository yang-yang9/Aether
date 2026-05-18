package com.zhousl.aether

import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhousl.aether.debug.TestProbe
import com.zhousl.aether.test.BridgeWebViewTestActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 4 — JS→Native bridge end-to-end. Verifies the white-box advantage:
 *
 * - JS in a WebView calls `AetherMarkdownHtmlBridge.updateNativeText("from_h5")`.
 * - The Native side observes the new value via [TestProbe].
 *
 * This is what 方案 A (Appium) approximates with marker-injection across
 * Context switches, and what 方案 C (Maestro) cannot do at all (no JS
 * execution, no Native state inspection). Here, no protocol hop is needed —
 * Espresso Web drives the click and Native code inspects [TestProbe]
 * directly.
 *
 * Implementation note: the bridge is attached via a stand-in object
 * ([BridgeWebViewTestActivity.AetherBridgeStub]) registered under the
 * production bridge name. Same `@JavascriptInterface` contract → same
 * end-to-end behavior. See the activity's KDoc for the rationale.
 */
@RunWith(AndroidJUnit4::class)
class BridgeIntegrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(BridgeWebViewTestActivity::class.java)

    @Before
    fun resetProbe() {
        TestProbe.update(null)
    }

    @Test
    fun jsClickPropagatesThroughBridgeToTestProbe() {
        onWebView()
            .withElement(findElement(Locator.ID, "trigger"))
            .perform(webClick())

        // Bridge dispatch is synchronous on the WebView thread, but state
        // observation from the test thread can race; poll briefly.
        val deadline = System.currentTimeMillis() + 5_000
        while (TestProbe.text.value != "from_h5" && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        assertEquals("from_h5", TestProbe.text.value)
    }
}
