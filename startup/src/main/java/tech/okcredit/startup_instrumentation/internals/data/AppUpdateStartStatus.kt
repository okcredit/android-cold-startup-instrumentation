package tech.okcredit.startup_instrumentation.internals.data

enum class AppUpdateStartStatus {
    FIRST_START_AFTER_CLEAR_DATA,
    FIRST_START_AFTER_FRESH_INSTALL,
    FIRST_START_AFTER_UPGRADE,
    NORMAL_START
}
