package tech.okcredit.startup_instrumentation.internals.data

import android.content.Intent
import tech.okcredit.startup_instrumentation.AppStartUpTracer

sealed class AppLaunchMetrics {

    data class ErrorRetrievingAppLaunchData(val throwable: Throwable) : AppLaunchMetrics()

    data class ColdStartUpData(
        /**
         * The Tech metrics regarding cold startup performance.
         */
        val startUpMetrics: AppStartUpTracer.AppStartUpMetrics,

        /**
         * The Info regarding app update.
         */
        val appUpdateData: AppUpdateData,

        /**
         * The First Activity name which opened first.
         */
        val firstActivityName: String?,

        /**
         * Return information about who launched the first activity.
         * See [android.app.Activity.getReferrer]
         */
        val firstActivityReferrer: String?,

        /**
         * The First Activity intent.
         */
        val firstActivityIntent: Intent?
    ) : AppLaunchMetrics()

    data class WarmAndHotStartUpData(
        /**
         * The Tech metrics regarding warm and hot startup performance.
         */
        val warmAndHotStartUpMetrics: WarmAndHotStartUpMetrics,

        /**
         * State of activity when user returns back to App.
         */
        val activityState: ActivityState,

        /**
         * The relative importance level that the system places on this process
         * See [android.app.ActivityManager.RunningAppProcessInfo.importance]
         */
        val importance: Int?,

        /**
         * Duration from last last app stop to resume
         */
        val durationFromLastAppStop: Long?,

        /**
         * Return name of first activity
         */
        val resumeActivityName: String?,

        /**
         * Return information about who launched the first activity.
         * See [android.app.Activity.getReferrer]
         */
        val resumeActivityReferrer: String?,

        /**
         * Return information about resumed activity intent.
         */
        val resumeActivityIntentData: Intent?,
    ) : AppLaunchMetrics()
}

data class WarmAndHotStartUpMetrics(
    val timeBetweenResumeToFirstDraw: Long,
    val timeBetweenCreatedToResume: Long?,
    val timeBetweenStartToResume: Long?,
)
