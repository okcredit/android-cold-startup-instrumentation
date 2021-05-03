package tech.okcredit.startup_instrumentation.internals

import tech.okcredit.startup_instrumentation.internals.NextDrawListener.Companion.onNextDraw
import android.app.Activity
import android.app.Application
import android.os.*
import androidx.annotation.RequiresApi
import tech.okcredit.startup_instrumentation.AppStartUpTrace

class AppStartMeasureLifeCycleCallBacks(private val responseCallback: (AppStartUpTrace.AppStartUpMetrics) -> Unit) : Application.ActivityLifecycleCallbacks {

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
                    AppStartUpTrace.firstDrawTime = SystemClock.uptimeMillis()

                    if (AppStartUpTrace.isValidAppStartUpMeasure()) {
                        responseCallback.invoke(AppStartUpTrace.AppStartUpMetrics())
                    }
                }
            }
    }
}
