package tech.okcredit.startup_instrumentation.internals

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.os.*
import androidx.annotation.RequiresApi
import tech.okcredit.startup_instrumentation.AppStartUpTracer
import tech.okcredit.startup_instrumentation.AppStartUpTracer.currentAppLaunchProcessed
import tech.okcredit.startup_instrumentation.AppStartUpTracer.isFirstPostExecuted
import tech.okcredit.startup_instrumentation.AppStartUpTracer.lastAppPauseTime
import tech.okcredit.startup_instrumentation.internals.GetAppUpdateAndLastProcessInfo.Companion.recordColdStartAndTrackAppUpgrade
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.createdActivityHashes
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.recordActivityCreated
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.recordActivityResumed
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.recordActivityStarted
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.resumedActivityHashes
import tech.okcredit.startup_instrumentation.internals.app_lifecycle.RecordOfActivityLifecycle.startedActivityHashes
import tech.okcredit.startup_instrumentation.internals.data.AppLaunchMetrics
import tech.okcredit.startup_instrumentation.internals.data.AppUpdateData
import tech.okcredit.startup_instrumentation.internals.data.ActivityState
import tech.okcredit.startup_instrumentation.internals.data.WarmAndHotStartUpMetrics
import tech.okcredit.startup_instrumentation.internals.utils.AppStartUpMeasurementUtils
import tech.okcredit.startup_instrumentation.internals.utils.AppStartUpMeasurementUtils.getProcessInfo
import tech.okcredit.startup_instrumentation.internals.utils.NextDrawListener.Companion.onNextDraw
import java.lang.IllegalStateException

@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class AppStartMeasureLifeCycleCallBacks(
    private val context: Application,
    private val firstDrawColdStartUpCallback: (AppStartUpTracer.AppStartUpMetrics) -> Unit,
    private val appLaunchCallback: (AppLaunchMetrics) -> Unit
) :
    Application.ActivityLifecycleCallbacks {

    private var firstDrawInvoked = false
    private var firstActivityCreated = false
    private var firstActivityResumed = false

    override fun onActivityPreResumed(activity: Activity) {
        recordActivityResumed(activity)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResumed(activity: Activity) {
        val identityHash = recordActivityResumed(activity)

        if (!firstActivityResumed) {
            firstActivityResumed = true
            AppStartUpTracer.firstActivityResumeTime = SystemClock.uptimeMillis()
        }


        AppStartUpMeasurementUtils.getSingleThreadExecutorForLaunchTracker().execute {
            val hadResumedActivity = resumedActivityHashes.size > 1
            if (!hadResumedActivity && !currentAppLaunchProcessed) {
                currentAppLaunchProcessed = true

                val processInfo: ActivityManager.RunningAppProcessInfo? =
                    activity.getProcessInfo()

                when {
                    isFirstPostExecuted -> { // Hot and Warm StartUp
                        val onCreateRecord = createdActivityHashes.getValue(identityHash)

                        val temperature = if (onCreateRecord.sameMessage) {
                            if (onCreateRecord.hasSavedState) {
                                ActivityState.CREATED_WITH_STATE
                            } else {
                                ActivityState.CREATED_NO_STATE
                            }
                        } else {
                            val onStartRecord = startedActivityHashes.getValue(identityHash)
                            if (onStartRecord.sameMessage) {
                                ActivityState.STARTED
                            } else {
                                ActivityState.RESUMED
                            }
                        }


                        activity.window?.decorView?.onNextDraw {
                            appLaunchCallback.invoke(
                                AppLaunchMetrics.WarmAndHotStartUpData(
                                    warmAndHotStartUpMetrics = WarmAndHotStartUpMetrics(
                                        timeBetweenResumeToFirstDraw = SystemClock.uptimeMillis() - resumedActivityHashes.getValue(
                                            identityHash
                                        ).start,
                                        timeBetweenCreatedToResume = resumedActivityHashes.getValue(
                                            identityHash
                                        ).start - createdActivityHashes.getValue(identityHash).start,
                                        timeBetweenStartToResume = resumedActivityHashes.getValue(
                                            identityHash
                                        ).start - startedActivityHashes.getValue(identityHash).start,
                                    ),
                                    activityState = temperature,
                                    durationFromLastAppStop = lastAppPauseTime?.let { SystemClock.uptimeMillis() - it },
                                    importance = processInfo?.importance,
                                    resumeActivityName = onCreateRecord.activityName,
                                    resumeActivityReferrer = onCreateRecord.referrer,
                                    resumeActivityIntent = onCreateRecord.intent
                                )
                            )
                        }
                    }
                    processInfo?.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> {
                        val appUpdateData: AppUpdateData = context.recordColdStartAndTrackAppUpgrade()

                        activity.window?.decorView?.onNextDraw {
                            AppStartUpTracer.firstDrawTime = SystemClock.uptimeMillis()

                            if (PreConditionStartUp.isValidAppStartUpMeasure()) {
                                appLaunchCallback.invoke(
                                    AppLaunchMetrics.ColdStartUpData(
                                        startUpMetrics = AppStartUpTracer.AppStartUpMetrics(),
                                        appUpdateData = appUpdateData,
                                        firstActivityIntent = AppStartUpTracer.firstActivityIntent,
                                        firstActivityName = AppStartUpTracer.firstActivityName,
                                        firstActivityReferrer = AppStartUpTracer.firstActivityReferrer,
                                    )
                                )
                            } else {
                                appLaunchCallback.invoke(
                                    AppLaunchMetrics.ErrorRetrievingAppLaunchData(
                                        IllegalStateException(PreConditionStartUp.findErrorReason())
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        resumedActivityHashes -= Integer.toHexString(System.identityHashCode(activity))
    }

    override fun onActivityPreStarted(activity: Activity) {
        recordActivityStarted(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        recordActivityStarted(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityHashes -= Integer.toHexString(System.identityHashCode(activity))
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        recordActivityCreated(activity, savedInstanceState)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        recordActivityCreated(activity, savedInstanceState)

        if (!firstActivityCreated) {
            firstActivityCreated = true

            AppStartUpTracer.firstActivityCreatedTime = SystemClock.uptimeMillis()
            AppStartUpTracer.firstActivityName = activity.localClassName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                AppStartUpTracer.firstActivityReferrer = activity.referrer.toString()
            }
            AppStartUpTracer.firstActivityIntent = activity.intent
        }

        if (!firstDrawInvoked) {
            activity.window?.decorView?.onNextDraw {
                if (firstDrawInvoked) return@onNextDraw
                firstDrawInvoked = true
                AppStartUpTracer.firstDrawTime = SystemClock.uptimeMillis()

                if (PreConditionStartUp.isValidAppStartUpMeasure()) {
                    firstDrawColdStartUpCallback.invoke(AppStartUpTracer.AppStartUpMetrics())
                }
            }
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        createdActivityHashes -= Integer.toHexString(System.identityHashCode(activity))
    }
}
