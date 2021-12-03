package tech.okcredit.appstartupinstrumentation

import android.app.Application
import android.os.StrictMode
import android.util.Log
import tech.okcredit.startup_instrumentation.AppStartUpTracer

class Application : Application() {
    override fun onCreate() {
        AppStartUpTracer.start() // Should be at the end of App.onCreate()
        super.onCreate()

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )

//        AppStartUpTracer.stop(this) { appStartUpMetrics-> //Should be at the end of App.onCreate()
//            Log.v("<<<<Cold StartUp", appStartUpMetrics.toString())
//        }

        AppStartUpTracer.onAppLaunchListener(this) { appStartUpMetrics -> // Should be at the end of App.onCreate()
            Log.v("<<<<App Launched", appStartUpMetrics.toString())
        }
    }
}
