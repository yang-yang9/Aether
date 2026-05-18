package com.zhousl.aether.test

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.widget.LinearLayout

/**
 * Minimal hostable test activity: shows a WebView with a fixed HTML payload
 * exercising form input + button click + JS-driven DOM mutation. Used by
 * [com.zhousl.aether.WebViewEspressoWebTest] to exercise Espresso Web's
 * DriverAtoms / Locator API in isolation from Aether's actual chat flow.
 */
class WebViewTestActivity : Activity() {
    lateinit var webView: WebView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
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

    companion object {
        private val HTML_PAYLOAD = """
            <!DOCTYPE html>
            <html>
              <body>
                <h1 id="title">Hello WebView</h1>
                <input id="search-input" type="text" />
                <button id="search-submit">Search</button>
                <div id="result">empty</div>
                <script>
                  document.getElementById('search-submit').onclick = function() {
                    var v = document.getElementById('search-input').value;
                    document.getElementById('result').textContent = 'searched: ' + v;
                  };
                </script>
              </body>
            </html>
        """.trimIndent()
    }
}
