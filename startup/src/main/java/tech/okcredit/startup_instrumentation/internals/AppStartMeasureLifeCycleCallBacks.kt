package tech.okcredit.startup_instrumentation.internals

import tech.okcredit.startup_instrumentation.internals.NextDrawListener.Companion.onNextDraw
import android.app.Activity
import android.app.Application
import android.os.*
import androidx.annotation.RequiresApi
import tech.okcredit.startup_instrumentation.AppStartUpTracer

class AppStartMeasureLifeCycleCallBacks(private val responseCallback: (AppStartUpTracer.AppStartUpMetrics) -> Unit) : Application.ActivityLifecycleCallbacks {

    var firstDrawInvoked = false

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (!firstDrawInvoked) {
                activity.window?.decorView?.onNextDraw {
                    if (firstDrawInvoked) return@onNextDraw
                    firstDrawInvoked = true
                    AppStartUpTracer.firstDrawTime = SystemClock.uptimeMillis()

                    if (AppStartUpTracer.isValidAppStartUpMeasure()) {
                        responseCallback.invoke(AppStartUpTracer.AppStartUpMetrics())
                    }
                }
            }
    }
}
