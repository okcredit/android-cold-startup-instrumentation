package tech.okcredit.startup_instrumentation.internals

import tech.okcredit.startup_instrumentation.AppStartUpTracer

internal object PreConditionStartUp {

    private const val COLD_LAUNCH_MAX_LIMIT = 30_000

    fun isValidAppStartUpMeasure(): Boolean {
        return AppStartUpTracer.processForkTime != 0L && AppStartUpTracer.contentProviderStartedTime != 0L && AppStartUpTracer.appOnCreateTime != 0L &&
            AppStartUpTracer.appOnCreateEndTime != 0L && AppStartUpTracer.firstDrawTime != 0L && AppStartUpTracer.firstDrawTime - AppStartUpTracer.processForkTime < COLD_LAUNCH_MAX_LIMIT
    }

    fun findErrorReason(): String {
        return when {
            AppStartUpTracer.processForkTime == 0L -> {
                "Not able to track process start time"
            }
            AppStartUpTracer.contentProviderStartedTime == 0L -> {
                "Not able to track content provider start time"
            }
            AppStartUpTracer.appOnCreateTime == 0L -> {
                "Not able to App created. Please make sure that AppStartUpTracer.start() added before super.onCreate() on App OnCreate."
            }
            AppStartUpTracer.appOnCreateEndTime == 0L -> {
                "Not able to track the end of App.onCreate()"
            }
            AppStartUpTracer.firstDrawTime == 0L -> {
                "Not able to track first draw"
            }
            AppStartUpTracer.firstDrawTime - AppStartUpTracer.processForkTime >= COLD_LAUNCH_MAX_LIMIT -> {
                "Process start to first draw is ${AppStartUpTracer.firstDrawTime - AppStartUpTracer.processForkTime}. " +
                    "it exceeded 30 Sec. check android.os.Process.getStartUptimeMillis() returning right values"
            }
            else -> {
                "unknown"
            }
        }
    }
}
