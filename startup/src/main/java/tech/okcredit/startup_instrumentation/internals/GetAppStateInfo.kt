package tech.okcredit.startup_instrumentation.internals

import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.SystemClock
import tech.okcredit.startup_instrumentation.internals.data.AppStateInfo
import tech.okcredit.startup_instrumentation.internals.data.AppUpdateStartStatus
import java.util.*

/**
 * Collect info regarding app updates like app starts after first install, an update, or a crash.
 */
internal class GetAppStateInfo private constructor(
    private val application: Application
) {

    private val preferences by lazy {
        application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun readAndUpdate(): AppStateInfo {
        val appPackageInfo = application.packageManager.getPackageInfo(application.packageName, 0)!!

        var allVersionNamesString: String
        var allVersionCodesString: String
        val status: AppUpdateStartStatus
        val crashedInLastProcess: Boolean?
        var lastCrashMessage: String? = null
        val lastProcessCrashElapsedRealtime: Long?
        val updatedOsSinceLastStart: Boolean?
        val versionName = appPackageInfo.versionName ?: "null"

        @Suppress("DEPRECATION")
        val longVersionCode = if (Build.VERSION.SDK_INT >= 28) {
            appPackageInfo.longVersionCode
        } else {
            appPackageInfo.versionCode.toLong()
        }
        val longVersionCodeString = longVersionCode.toString()

        if (!preferences.contains(VERSION_NAME_KEY)) {
            status = if (appPackageInfo.firstInstallTime != appPackageInfo.lastUpdateTime) {
                crashedInLastProcess = null
                updatedOsSinceLastStart = null

                AppUpdateStartStatus.FIRST_START_AFTER_CLEAR_DATA
            } else {
                crashedInLastProcess = false
                updatedOsSinceLastStart = false

                AppUpdateStartStatus.FIRST_START_AFTER_FRESH_INSTALL
            }
            allVersionNamesString = versionName
            allVersionCodesString = longVersionCodeString
        } else {
            val previousLongVersionCode = if (preferences.contains(LONG_VERSION_CODE_KEY)) {
                preferences.getLong(LONG_VERSION_CODE_KEY, -1)
            } else {
                preferences.getInt(VERSION_CODE_KEY, -1)
            }
            allVersionNamesString =
                preferences.getString(ALL_VERSION_NAMES_KEY, versionName)!!
            allVersionCodesString =
                preferences.getString(ALL_VERSION_CODES_KEY, longVersionCodeString)!!

            if (previousLongVersionCode != longVersionCode) {
                status = AppUpdateStartStatus.FIRST_START_AFTER_UPGRADE
                allVersionNamesString = "$versionName, $allVersionNamesString"
                allVersionCodesString = "$longVersionCodeString, $allVersionCodesString"
            } else {
                status = AppUpdateStartStatus.NORMAL_START
            }

            updatedOsSinceLastStart =
                preferences.getString(BUILD_FINGERPRINT_KEY, UNKNOWN_BUILD_FINGERPRINT)!!
                    .let { fingerprint ->
                        if (fingerprint == UNKNOWN_BUILD_FINGERPRINT) {
                            null
                        } else fingerprint != Build.FINGERPRINT
                    }

            lastProcessCrashElapsedRealtime = preferences.getLong(CRASH_REALTIME_KEY, UNKNOWN_CRASH)

            crashedInLastProcess = if (lastProcessCrashElapsedRealtime == UNKNOWN_CRASH) {
                null
            } else {
                lastCrashMessage = preferences.getString(CRASH_MESSAGE, null)
                lastProcessCrashElapsedRealtime != NO_CRASH
            }
        }

        preferences.edit()
            .putLong(LONG_VERSION_CODE_KEY, longVersionCode)
            .putString(VERSION_NAME_KEY, versionName)
            .putString(ALL_VERSION_NAMES_KEY, allVersionNamesString)
            .putString(ALL_VERSION_CODES_KEY, allVersionCodesString)
            .putLong(CRASH_REALTIME_KEY, NO_CRASH)
            .putString(BUILD_FINGERPRINT_KEY, Build.FINGERPRINT)
            .apply()

        val allVersionNames = allVersionNamesString.split(", ")
        val allVersionCodes = allVersionCodesString.split(", ")
            .map { it.toInt() }

        val lastColdLaunchTime = preferences.getLong(LAST_COLD_LAUNCH_TIME, -1L)
        val lastActivityTime = preferences.getLong(LAST_ACTIVITY_TIME, 0L)
        val lastExitInformation = getLastExitInformation()

        return AppStateInfo(
            status = status,
            firstInstallTimeMillis = appPackageInfo.firstInstallTime,
            lastUpdateTimeMillis = appPackageInfo.lastUpdateTime,
            lastActiveTime = lastActivityTime,
            lastColdLaunchTimeMillis = lastColdLaunchTime,
            allInstalledVersionNames = allVersionNames,
            allInstalledVersionCodes = allVersionCodes,
            crashedInLastProcess = crashedInLastProcess,
            lastCrashMessage = lastCrashMessage,
            updatedOsSinceLastStart = updatedOsSinceLastStart,
            lastExitInformation = lastExitInformation
        )
    }

    private fun getLastExitInformation(): ApplicationExitInfo? {
        val am = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val exitList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            am.getHistoricalProcessExitReasons(application.packageName, 0, 1)
        } else {
            return null
        }
        if (exitList.isEmpty()) {
            return null
        }
        return exitList.first()
    }

    private fun onAppCrashing(exception: Throwable) {
        preferences.edit()
            .putLong(CRASH_REALTIME_KEY, SystemClock.elapsedRealtime())
            .putString(CRASH_MESSAGE, exception.message)
            .apply()
    }

    private fun recordColdStart() {
        preferences.edit()
            .putLong(LAST_COLD_LAUNCH_TIME, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREF_NAME = "appUpgradeInfoPref"
        private const val VERSION_CODE_KEY = "app_version_code"
        private const val LONG_VERSION_CODE_KEY = "app_long_version_code"
        private const val VERSION_NAME_KEY = "app_version_name"
        private const val ALL_VERSION_NAMES_KEY = "app_all_version_names"
        private const val ALL_VERSION_CODES_KEY = "app_all_version_codes"
        private const val LAST_ACTIVITY_TIME = "last_activity_time"
        private const val CRASH_REALTIME_KEY = "crash_realtime"
        private const val CRASH_MESSAGE = "crash_message"
        private const val BUILD_FINGERPRINT_KEY = "build_fingerprint"
        private const val LAST_COLD_LAUNCH_TIME = "last_cold_launch_time"
        private const val NO_CRASH = -1L
        private const val UNKNOWN_CRASH = -2L
        private const val UNKNOWN_BUILD_FINGERPRINT = "UNKNOWN_BUILD_FINGERPRINT"

        fun Application.recordColdStartAndTrackAppUpgrade(): AppStateInfo {
            val detector = GetAppStateInfo(this)

            val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                detector.onAppCrashing(exception)
                defaultExceptionHandler?.uncaughtException(thread, exception)
            }

            val data = detector.readAndUpdate()
            detector.recordColdStart()
            return data
        }

        fun Application.recordLastActivity() {
            val preferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    preferences.edit()
                        .putLong(LAST_ACTIVITY_TIME, System.currentTimeMillis())
                        .apply()
                }
            }, 1000)
        }
    }
}
