package com.zhousl.aether.debug

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.zhousl.aether.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test instrumentation hook for the native-frameworks POC. WebView Bridge
 * methods write to this singleton; the [TestProbeOverlay] composable mounts a
 * 1dp-invisible Text with a stable testTag so instrumentation tests can assert
 * the latest value via Compose-Test or Espresso.
 *
 * Production-inert: [update] is a no-op outside DEBUG, and the overlay early-
 * returns. Release APK is unaffected.
 */
object TestProbe {
    const val OverlayTestTag = "test-probe-text"

    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text.asStateFlow()

    fun update(value: String?) {
        if (BuildConfig.DEBUG) {
            _text.value = value
        }
    }
}

@Composable
fun TestProbeOverlay() {
    if (!BuildConfig.DEBUG) return
    val current by TestProbe.text.collectAsState()
    Text(
        text = current.orEmpty(),
        modifier = Modifier
            .alpha(0f)
            .size(1.dp)
            .testTag(TestProbe.OverlayTestTag),
    )
}
