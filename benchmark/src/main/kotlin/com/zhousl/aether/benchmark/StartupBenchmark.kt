package com.zhousl.aether.benchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7a — cold-start latency baseline. Macrobenchmark drives `pressHome()`
 * + `startActivityAndWait()` for `iterations` times and records
 * `StartupTimingMetric` (firstFrame / firstActivityCommitted / fullyDrawn).
 *
 * 方案 A (Appium): perf-baseline.spec.ts approximates cold start via
 *   `terminate + activate + Date.now()` deltas — coarser, noisier.
 * 方案 B (this): Macrobenchmark hooks into the platform tracing stack —
 *   accurate to ~ms, with multi-iteration statistics built in.
 * 方案 C (Maestro): no perf measurement at all.
 *
 * Note: ideally run against a non-debuggable / minified variant; here we
 * target debug for POC simplicity. JSON output suffices to demonstrate the
 * framework integration; production usage would add a `benchmark` buildType
 * with debug signing + isDebuggable=false.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStart() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.baimoqilin.aether"
    }
}
