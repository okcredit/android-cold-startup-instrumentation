package tech.okcredit.appstartupinstrumentation

import android.app.Application
import android.util.Log
import tech.okcredit.startup_instrumentation.AppStartUpTrace

class Application: Application() {
    override fun onCreate() {
        AppStartUpTrace.start()
        super.onCreate()

        AppStartUpTrace.stop(this) { appStartUpMetrics->
            Log.v("StartUp Logs", appStartUpMetrics.toString())
        }
    }
}