package tech.okcredit.startup_instrumentation.internals.app_lifecycle

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import tech.okcredit.startup_instrumentation.AppStartUpTracer.currentAppLaunchProcessed
import tech.okcredit.startup_instrumentation.AppStartUpTracer.isFirstPostExecuted
import tech.okcredit.startup_instrumentation.AppStartUpTracer.lastAppPauseTime

internal object ProcessLifecycleHandler {

    fun updateAppLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onAppStart() {
                currentAppLaunchProcessed = false
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onAppStop() {
                currentAppLaunchProcessed = true
                lastAppPauseTime = SystemClock.uptimeMillis()
            }
        })

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            isFirstPostExecuted = true
        }
    }
}
