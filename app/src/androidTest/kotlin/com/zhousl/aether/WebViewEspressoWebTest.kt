package com.zhousl.aether

import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhousl.aether.test.WebViewTestActivity
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 3 — Espresso Web baseline. Verifies the white-box framework can:
 *
 * 1. Locate a DOM element by CSS-equivalent ID inside a WebView.
 * 2. Read the element's text via DriverAtoms.getText.
 * 3. Type into an input via DriverAtoms.webKeys.
 * 4. Click a button via DriverAtoms.webClick.
 * 5. Assert a JS-mutated element reflects the typed value.
 *
 * Compared with 方案 A (Appium): Appium switches Context to WEBVIEW and uses
 * `$(...)` selectors. Here we stay in the Espresso process; Espresso Web
 * proxies to WebView via JS injection — no protocol hop.
 *
 * Compared with 方案 C (Maestro): Maestro can't do this at all — it can only
 * tap visible text, not address DOM nodes.
 */
@RunWith(AndroidJUnit4::class)
class WebViewEspressoWebTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(WebViewTestActivity::class.java)

    @Test
    fun canReadAndDriveDom() {
        // 1) Read the static title.
        onWebView()
            .withElement(findElement(Locator.ID, "title"))
            .check(webMatches(getText(), containsString("Hello WebView")))

        // 2) Type into the input field.
        onWebView()
            .withElement(findElement(Locator.ID, "search-input"))
            .perform(webKeys("aether"))

        // 3) Click the submit button (JS mutates #result).
        onWebView()
            .withElement(findElement(Locator.ID, "search-submit"))
            .perform(webClick())

        // 4) Assert the JS handler updated #result.
        onWebView()
            .withElement(findElement(Locator.ID, "result"))
            .check(webMatches(getText(), containsString("searched: aether")))
    }
}
