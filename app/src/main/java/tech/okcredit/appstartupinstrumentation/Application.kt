package tech.okcredit.appstartupinstrumentation

import android.app.Application
import android.util.Log
import tech.okcredit.startup_instrumentation.AppStartUpTracer

class Application: Application() {
    override fun onCreate() {
        AppStartUpTracer.start() //Should be at the end of App.onCreate()
        super.onCreate()

        AppStartUpTracer.stop(this) { appStartUpMetrics-> //Should be at the end of App.onCreate()
            Log.v("StartUp Logs", appStartUpMetrics.toString())
        }
    }
}