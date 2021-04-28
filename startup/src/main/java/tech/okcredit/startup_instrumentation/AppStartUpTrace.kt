package tech.okcredit.startup_instrumentation

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.SystemClock
import org.jetbrains.annotations.NonNls
import tech.okcredit.startup_instrumentation.internals.AppStartMeasureLifeCycleCallBacks
import tech.okcredit.startup_instrumentation.internals.AppStartUpMeasurementUtils
import java.util.concurrent.Executors

object AppStartUpTrace {

    var processForkTime = 0L
    var contentProviderStartedTime = 0L
    var appOnCreateTime = 0L
    var appOnCreateEndTime = 0L
    var firstDrawTime = 0L

    data class Response(
        val totalTime: Long = firstDrawTime - processForkTime,
        val processForkToContentProvider: Long = contentProviderStartedTime - processForkTime,
        val contentProviderToAppStart: Long = appOnCreateTime - contentProviderStartedTime,
        val applicationOnCreateTime: Long = appOnCreateEndTime - appOnCreateTime,
        val appOnCreateEndToFirstDraw: Long = firstDrawTime - appOnCreateEndTime
    ) {
        @NonNls
        override fun toString(): String {
            return """Cold StartUp Time : $totalTime
            PROCESS_FORK_TO_CONTENT_PROVIDER  : $processForkToContentProvider
            CONTENT_PROVIDER_TO_APP_START: $contentProviderToAppStart
            APP_ON_CREATE_TIME: $applicationOnCreateTime
            APP_ON_CREATE_END_TO_FIRST_DRAW: $appOnCreateEndToFirstDraw
        """
        }
    }


    /**
     * Uses for tracking App OnCreate Start. Call this before super.onCreate() on App OnCreate.
     */
    fun start() {
        appOnCreateTime = SystemClock.uptimeMillis()
    }


    /**
     * Uses for tracking App OnCreate Stop. Call this at the end of App OnCreate.
     */
    fun stop(context: Context, responseCallback: (Response) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Executors.newSingleThreadExecutor().execute {
                val appStartMeasureLifeCycleCallBacks = AppStartMeasureLifeCycleCallBacks(responseCallback)
                (context.applicationContext as Application).registerActivityLifecycleCallbacks(appStartMeasureLifeCycleCallBacks)

                processForkTime = AppStartUpMeasurementUtils.getProcessForkTime()
                appOnCreateEndTime = SystemClock.uptimeMillis()

            }
        }
    }

    // since measure time intervals using SystemClock.uptimeMillis() sometimes bind application starts then halts
    // mid way and the actual app start is much later. so adding a 30 sec limit for filtering those out.
    fun isValidAppStartUpMeasure(): Boolean {
        return processForkTime != 0L && contentProviderStartedTime != 0L && appOnCreateTime != 0L &&
                appOnCreateEndTime != 0L && firstDrawTime != 0L && firstDrawTime - processForkTime < 30_000
    }
}


