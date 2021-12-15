package tech.okcredit.startup_instrumentation

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import org.jetbrains.annotations.NonNls
import tech.okcredit.startup_instrumentation.internals.AppStartMeasureLifeCycleCallBacks
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.ProcessLifecycleHandler
import tech.okcredit.startup_instrumentation.internals.data.AppLaunchMetrics
import tech.okcredit.startup_instrumentation.internals.utils.AppStartUpMeasurementUtils
import tech.okcredit.startup_instrumentation.internals.utils.AppStartUpMeasurementUtils.getSingleThreadExecutorForLaunchTracker

/**
 * Singleton object centralizing all app start metrics and data.
 */
object AppStartUpTracer {

    /**
     * The SystemClock.uptimeMillis() at which this process was started
     */
    var processForkTime = 0L

    /**
     * The SystemClock.uptimeMillis() at which content provider is started.
     * Tracking from [tech.okcredit.startup_instrumentation.internals.AppStartContentProvider] which initialize first ContentProvider.
     */
    var contentProviderStartedTime: Long = 0L

    /**
     * The SystemClock.uptimeMillis() at which start of App.OnCrate.
     * Tracking from [tech.okcredit.startup_instrumentation.AppStartUpTracer.start]
     */
    var appOnCreateTime = 0L

    /**
     * The SystemClock.uptimeMillis() at which end of App.OnCrate.
     */
    var appOnCreateEndTime = 0L

    /**
     * The SystemClock.uptimeMillis() of first frame draw
     */
    var firstDrawTime = 0L

    /**
     * The SystemClock.uptimeMillis() of first activity created time
     */
    var firstActivityCreatedTime = 0L

    /**
     * First Activity name
     */
    var firstActivityName: String? = null

    /**
     * Return information about who launched the first activity.
     * See [android.app.Activity.getReferrer]
     */
    var firstActivityReferrer: String? = null

    /**
     * Intent of first activity
     */
    var firstActivityIntent: Intent? = null

    /**
     * The SystemClock.uptimeMillis() of first activity resume
     */
    var firstActivityResumeTime = 0L

    /**
     * Return has AppLaunch Processed after app resume.
     */
    internal var currentAppLaunchProcessed = true

    /**
     * Return First Handler.post() executed.
     */
    var isFirstPostExecuted = false

    /**
     * Return the last App Pause time in millis.
     */
    var lastAppPauseTime: Long? = null

    data class AppStartUpMetrics(
        val totalTime: Long = firstDrawTime - processForkTime,
        val processForkToContentProvider: Long = contentProviderStartedTime - processForkTime,
        val contentProviderToAppStart: Long = appOnCreateTime - contentProviderStartedTime,
        val applicationOnCreateTime: Long = appOnCreateEndTime - appOnCreateTime,
        val appOnCreateEndToFirstActivityCreate: Long = firstActivityCreatedTime - appOnCreateEndTime,
        val firstActivityCreateToResume: Long = firstActivityResumeTime - firstActivityCreatedTime,
        val firstActivityCreateToDraw: Long = firstDrawTime - firstActivityCreatedTime,
        val firstActivityResumeToDraw: Long = firstDrawTime - firstActivityResumeTime,
        val appOnCreateEndToFirstDraw: Long = firstDrawTime - appOnCreateEndTime
    ) {
        @NonNls
        override fun toString(): String {
            return """Cold StartUp Time : $totalTime
            PROCESS_FORK_TO_CONTENT_PROVIDER  : $processForkToContentProvider
            CONTENT_PROVIDER_TO_APP_START: $contentProviderToAppStart
            APP_ON_CREATE_TIME: $applicationOnCreateTime
            APP_ON_CREATE_END_TO_FIRST_ACTIVITY_CREATE: $appOnCreateEndToFirstActivityCreate,
            FIRST_ACTIVITY_CREATE_TO_RESUME: $firstActivityCreateToResume,
            FIRST_ACTIVITY_CREATE_TO_DRAW: $firstActivityCreateToDraw,
            FIRST_ACTIVITY_RESUME_TO_DRAW: $firstActivityResumeToDraw,
            APP_ON_CREATE_END_TO_FIRST_DRAW: $appOnCreateEndToFirstDraw,
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
    @Deprecated("No Longer Used, Use new onAppLaunchListeners() method", ReplaceWith("AppStartUpTracer.onAppLaunchListeners(context, callback)", "tech.okcredit.startup_instrumentation.AppStartUpTracer"))
    fun stop(context: Application, responseCallback: (AppStartUpMetrics) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSingleThreadExecutorForLaunchTracker().execute {
                val appStartMeasureLifeCycleCallBacks = AppStartMeasureLifeCycleCallBacks(
                    context = context,
                    firstDrawColdStartUpCallback = responseCallback,
                    appLaunchCallback = {}
                )
                context.registerActivityLifecycleCallbacks(appStartMeasureLifeCycleCallBacks)

                appOnCreateEndTime = SystemClock.uptimeMillis()
                setProcessData()
            }
        }
    }

    /**
     * Uses for tracking AppLaunch. Call this at the end of App OnCreate.
     */
    fun onAppLaunchListener(context: Application, responseCallback: (AppLaunchMetrics) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ProcessLifecycleHandler.updateAppLifecycle()

            getSingleThreadExecutorForLaunchTracker().execute {
                val appStartMeasureLifeCycleCallBacks = AppStartMeasureLifeCycleCallBacks(
                    context = context,
                    firstDrawColdStartUpCallback = {},
                    appLaunchCallback = responseCallback
                )

                context.registerActivityLifecycleCallbacks(appStartMeasureLifeCycleCallBacks)

                appOnCreateEndTime = SystemClock.uptimeMillis()
                setProcessData()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setProcessData() {
        processForkTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Process.getStartUptimeMillis()
        } else {
            AppStartUpMeasurementUtils.getProcessForkTime()
        }
        /***
         * https://dev.to/pyricau/android-vitals-when-did-my-app-start-24p4
         * Process.getStartUptimeMillis() is sometimes way off.
         * The interval between content provider start was greater
         * than 30 sec for 0.5% of app starts.
         * This might be due to some systems keeping a pool
         * of pre forked zygotes to accelerate app start.
         * falling back process Start time to contentProvider StartedTime.
         */
        Log.d("<<<<Diff", "is ${contentProviderStartedTime - processForkTime}")
        if (contentProviderStartedTime - processForkTime > 30_000) {
            processForkTime = contentProviderStartedTime
        }
    }
}
