package com.zhousl.aether

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 2 — native baseline. Exercises both halves of the white-box stack:
 *
 * - **UIAutomator** dismisses the system POST_NOTIFICATIONS dialog if it
 *   appears (Aether requests it from MainActivity.onCreate on API 33+).
 * - **Compose UI Test** finds the onboarding "Welcome" text and clicks the
 *   primary action.
 *
 * This is the analogue of:
 *   - 方案 A: poc/appium-android/tests/apidemos/functional/native-basic.spec.ts
 *   - 方案 C: poc/maestro-android/flows/apidemos/native-basic.yaml
 *
 * Notable difference from those POCs: the UI is Compose-only, so we use
 * `composeRule.onNodeWithText` rather than Espresso `onView(withText(…))`.
 */
@RunWith(AndroidJUnit4::class)
class NativeBasicTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun launchesAndAdvancesPastWelcome() {
        dismissNotificationPermissionIfPresent()

        val welcomeMatchers = listOf("Welcome to Aether", "欢迎使用 Aether")
        val getStartedMatchers = listOf("Get started", "开始使用")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            welcomeMatchers.any {
                composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
            }
        }

        val activeGetStarted = getStartedMatchers.firstOrNull {
            composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
        } ?: error("Neither 'Get started' nor '开始使用' button visible.")
        composeRule.onNodeWithText(activeGetStarted).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            welcomeMatchers.all {
                composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isEmpty()
            }
        }
    }

    /**
     * If the system POST_NOTIFICATIONS permission dialog is shown (API 33+),
     * dismiss it by clicking whichever "deny" label is present in the current
     * locale. We deny rather than allow because the test doesn't need real
     * notifications and denying is reversible across runs.
     */
    private fun dismissNotificationPermissionIfPresent() {
        device.wait(Until.hasObject(By.pkg("com.android.permissioncontroller")), 3_000)
        val denyLabels = listOf("Don’t allow", "Don't allow", "Deny", "不允许", "拒绝")
        for (label in denyLabels) {
            val obj = device.findObject(By.text(label)) ?: continue
            obj.click()
            return
        }
    }
}
