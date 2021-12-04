package tech.okcredit.startup_instrumentation.internals.data

import android.app.ApplicationExitInfo
import android.os.Build

/**
 * Info regarding app updates like app starts after first install, an update, or a crash.
 */
data class AppUpdateData(
    val status: AppUpdateStartStatus,

    /**
     * See [android.content.pm.PackageInfo.firstInstallTime]
     */
    val firstInstallTimeMillis: Long,

    /**
     * See [android.content.pm.PackageInfo.lastUpdateTime]
     */
    val lastUpdateTimeMillis: Long,

    /**
     * Last cold startup time
     */
    val lastColdLaunchTimeMillis: Long,

    /**
     * List of all [android.content.pm.PackageInfo.versionName] values for all installs of the app,
     * most recent first.
     */
    val allInstalledVersionNames: List<String>,

    /**
     * List of all [android.content.pm.PackageInfo.versionCode] values for all installs of the app,
     * most recent first.
     */
    val allInstalledVersionCodes: List<Int>,

    /**
     * Whether the app ran into Java crash after the last app start.
     *
     * Always false when [status] is [AppUpdateStartStatus.FIRST_START_AFTER_FRESH_INSTALL] (no prior
     * app start).
     *
     * Null if we couldn't determine when the app last crashed.
     */
    val crashedInLastProcess: Boolean?,

    /**
     * Message of crash if [crashedInLastProcess] is true
     *
     * Null if we couldn't determine when the app last crashed.
     */
    val lastCrashMessage: String?,

    /**
     * Whether the device OS was updated since the last app start, ie whether
     * [android.os.Build.FINGERPRINT] changed.
     *
     * Always false when [status] is [AppUpdateStartStatus.FIRST_START_AFTER_FRESH_INSTALL] (no prior
     * app start).
     *
     * Null if we hadn't saved the fingerprint in the last app start.
     */
    val updatedOsSinceLastStart: Boolean?,

    /**
     * Describes the information of an application process's death. it only available above API 29
     * see [android.app.ApplicationExitInfo]
     */
    val lastExitInformation: ApplicationExitInfo?
) {
    /**
     * Return last exit info reason
     */
    fun getLastExitReason(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || lastExitInformation == null) {
            return "NONE"
        }

        when (lastExitInformation.reason) {
            ApplicationExitInfo.REASON_UNKNOWN -> {
                return "UNKNOWN"
            }
            ApplicationExitInfo.REASON_EXIT_SELF -> {
                return "EXIT_SELF"
            }
            ApplicationExitInfo.REASON_SIGNALED -> {
                return "SIGNALED"
            }
            ApplicationExitInfo.REASON_LOW_MEMORY -> {
                return "LOW_MEMORY"
            }
            ApplicationExitInfo.REASON_CRASH -> {
                return "CRASH"
            }
            ApplicationExitInfo.REASON_CRASH_NATIVE -> {
                return "CRASH_NATIVE"
            }
            ApplicationExitInfo.REASON_ANR -> {
                return "ANR"
            }
            ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> {
                return "INITIALIZATION_FAILURE"
            }
            ApplicationExitInfo.REASON_PERMISSION_CHANGE -> {
                return "PERMISSION_CHANGE"
            }
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> {
                return "EXCESSIVE_RESOURCE_USAGE"
            }
            ApplicationExitInfo.REASON_USER_REQUESTED -> {
                return "USER_REQUESTED"
            }
            ApplicationExitInfo.REASON_USER_STOPPED -> {
                return "USER_STOPPED"
            }
            ApplicationExitInfo.REASON_OTHER -> {
                return "OTHER"
            }
            else -> {
                return "UNKNOWN_REASON"
            }
        }
    }
}

