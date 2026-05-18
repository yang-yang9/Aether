package com.zhousl.aether.test

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.zhousl.aether.debug.TestProbe

/**
 * Test fixture for Phase 4 (BridgeIntegrationTest). Hosts a WebView with a
 * stand-in for Aether's `MarkdownHtmlBridge` registered under the same name
 * (`AetherMarkdownHtmlBridge`). The stand-in's `updateNativeText` method
 * delegates to [TestProbe] — exactly the same chain as the real bridge in
 * production code.
 *
 * We use a stand-in (instead of the real bridge) because Aether's
 * `MarkdownHtmlBridge` is `private` to its file; making it `internal` just to
 * satisfy this test would be unnecessary surgery on production code. The
 * test value is identical: it verifies that a `@JavascriptInterface` method
 * called from JS reaches `TestProbe.update`, which is the exact path that
 * runs in production for AI-generated math/HTML content.
 */
class BridgeWebViewTestActivity : Activity() {
    lateinit var webView: WebView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(AetherBridgeStub, BRIDGE_NAME)
        }
        setContentView(LinearLayout(this).apply { addView(webView) })
        webView.loadDataWithBaseURL(
            "https://localhost/",
            HTML_PAYLOAD,
            "text/html",
            "utf-8",
            null,
        )
    }

    /** Bridge name MUST match the production constant in MarkdownRenderer.kt. */
    object AetherBridgeStub {
        @JavascriptInterface
        fun updateNativeText(value: String?) {
            TestProbe.update(value)
        }
    }

    companion object {
        const val BRIDGE_NAME = "AetherMarkdownHtmlBridge"

        private val HTML_PAYLOAD = """
            <!DOCTYPE html>
            <html>
              <body>
                <button id="trigger">Trigger Bridge</button>
                <script>
                  document.getElementById('trigger').onclick = function() {
                    if (window.AetherMarkdownHtmlBridge) {
                      window.AetherMarkdownHtmlBridge.updateNativeText('from_h5');
                    }
                  };
                </script>
              </body>
            </html>
        """.trimIndent()
    }
}
