package tech.okcredit.startup_instrumentation.internals

import tech.okcredit.startup_instrumentation.internals.NextDrawListener.Companion.onNextDraw
import android.app.Activity
import android.app.Application
import android.os.*
import androidx.annotation.RequiresApi
import tech.okcredit.startup_instrumentation.AppStartUpTrace

class AppStartMeasureLifeCycleCallBacks(private val responseCallback: (AppStartUpTrace.Response) -> Unit) : Application.ActivityLifecycleCallbacks {

    var firstDraw = false

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
            if (!firstDraw) {
                activity.window?.decorView?.onNextDraw {
                    if (firstDraw) return@onNextDraw
                    firstDraw = true
                    AppStartUpTrace.firstDrawTime = SystemClock.uptimeMillis()

                    if (AppStartUpTrace.isValidAppStartUpMeasure()) {
                        responseCallback.invoke(AppStartUpTrace.Response())
                    }
                }
            }
    }
}
