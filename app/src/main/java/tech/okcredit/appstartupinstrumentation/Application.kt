package tech.okcredit.appstartupinstrumentation

import android.app.Application
import android.os.Build
import android.util.Log
import tech.okcredit.startup_instrumentation.AppStartUpTracer

class Application : Application() {
    override fun onCreate() {
        AppStartUpTracer.start() // Should be at the end of App.onCreate()
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppStartUpTracer.onAppLaunchListener(this) { appStartUpMetrics -> // Should be at the end of App.onCreate()
                Log.v("<<<<App Launched", appStartUpMetrics.toString())
            }
        }
    }
}
