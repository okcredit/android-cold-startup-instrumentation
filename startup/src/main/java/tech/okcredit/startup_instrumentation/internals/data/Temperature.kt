package tech.okcredit.startup_instrumentation.internals.data

enum class Temperature {
    /**
     * Warm start: the activity was created with no state bundle and then resumed.
     */
    CREATED_NO_STATE,

    /**
     * Warm start: the activity was created with a state bundle and then resumed.
     */
    CREATED_WITH_STATE,

    /**
     * A hot start: the activity was started and then resumed
     */
    STARTED,

    /**
     * A hot start: the activity was resumed.
     */
    RESUMED
}
