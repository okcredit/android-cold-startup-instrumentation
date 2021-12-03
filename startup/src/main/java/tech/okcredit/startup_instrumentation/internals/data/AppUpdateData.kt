package tech.okcredit.startup_instrumentation.internals.data

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
)
