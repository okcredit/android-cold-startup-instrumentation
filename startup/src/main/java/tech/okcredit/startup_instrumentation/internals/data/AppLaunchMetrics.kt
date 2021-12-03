package tech.okcredit.startup_instrumentation.internals.data

import android.content.Intent
import tech.okcredit.startup_instrumentation.AppStartUpTracer

sealed class AppLaunchMetrics {

    data class ErrorRetrievingAppLaunchData(val throwable: Throwable) : AppLaunchMetrics()

    data class ColdStartUpData(
        val startUpMetrics: AppStartUpTracer.AppStartUpMetrics,
        val appUpdateData: AppUpdateData,
        val firstActivityName: String?,
        val firstActivityReferrer: String?,
        val firstActivityIntent: Intent?
    ) : AppLaunchMetrics()

    data class WarmAndColdStartUpData(
        val temperature: Temperature,
        val importance: Int?,
        val durationFromLastAppStop: Long?,
        val timeBetweenResumeToFirstDraw: Long,
        val timeBetweenCreatedToResume: Long?,
        val timeBetweenStartToResume: Long?,

        val resumeActivityName: String?,
        val resumeActivityReferrer: String?,
        val resumeActivityIntentData: Intent?,
    ) : AppLaunchMetrics()
}
