package com.zhousl.aether.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7b — interactive frame timing. Drives a swipe gesture on the
 * Welcome / Onboarding screen via UIAutomator's coordinate-based swipe and
 * records `FrameTimingMetric` (P50/P90/P95/P99 + jank).
 *
 * The Welcome screen has limited scrollable content, so the swipe is mostly
 * a synthetic input event. Subsequent phases (after API-key onboarding is
 * automatable) can target the chat list which is genuinely scrollable.
 *
 * 方案 A perf-baseline.spec.ts uses `dumpsys gfxinfo` parsed text — a
 * heuristic. FrameTimingMetric here reads the actual platform trace.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun gestureFrames() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 3,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
    ) {
        val w = device.displayWidth
        val h = device.displayHeight
        device.swipe(
            /* startX = */ w / 2,
            /* startY = */ h * 3 / 4,
            /* endX = */ w / 2,
            /* endY = */ h / 4,
            /* steps = */ 20,
        )
        device.waitForIdle()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.baimoqilin.aether"
    }
}
