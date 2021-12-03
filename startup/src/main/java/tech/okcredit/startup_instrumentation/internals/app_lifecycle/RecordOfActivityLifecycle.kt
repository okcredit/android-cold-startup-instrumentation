package tech.okcredit.startup_instrumentation.internals.app_lifecycle

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock

internal object RecordOfActivityLifecycle {

    class OnCreateRecord(
        val sameMessage: Boolean,
        val hasSavedState: Boolean,
        val referrer: String?,
        val activityName: String,
        val intent: Intent?,
        val start: Long
    )

    class OnStartRecord(val sameMessage: Boolean, val start: Long)
    class OnResumeRecord(val start: Long)

    val createdActivityHashes = mutableMapOf<String, OnCreateRecord>()
    val startedActivityHashes = mutableMapOf<String, OnStartRecord>()
    val resumedActivityHashes = mutableMapOf<String, OnResumeRecord>()

    fun recordActivityResumed(activity: Activity): String {
        val start = SystemClock.uptimeMillis()
        val identityHash = Integer.toHexString(System.identityHashCode(activity))
        if (identityHash in resumedActivityHashes) {
            return identityHash
        }
        resumedActivityHashes[identityHash] = OnResumeRecord(start)
        return identityHash
    }

    fun recordActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        val identityHash = Integer.toHexString(System.identityHashCode(activity))
        if (identityHash in createdActivityHashes) {
            return
        }

        val start = SystemClock.uptimeMillis()
        val referrer: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            activity.referrer.toString()
        } else {
            null
        }

        val hasSavedStated = savedInstanceState != null
        createdActivityHashes[identityHash] =
            OnCreateRecord(
                sameMessage = true,
                hasSavedState = hasSavedStated,
                referrer = referrer,
                activityName = activity.localClassName,
                intent = activity.intent,
                start = start
            )

        RecordOfActivityHandlerJobs.joinPost {
            if (identityHash in createdActivityHashes) {
                createdActivityHashes[identityHash] = OnCreateRecord(
                    sameMessage = false,
                    hasSavedState = hasSavedStated,
                    referrer = referrer,
                    activityName = activity.localClassName,
                    intent = activity.intent,
                    start = start
                )
            }
        }
    }

    fun recordActivityStarted(activity: Activity) {
        val start = SystemClock.uptimeMillis()
        val identityHash = Integer.toHexString(System.identityHashCode(activity))
        if (identityHash in startedActivityHashes) {
            return
        }
        startedActivityHashes[identityHash] = OnStartRecord(true, start)
        RecordOfActivityHandlerJobs.joinPost {
            if (identityHash in startedActivityHashes) {
                startedActivityHashes[identityHash] = OnStartRecord(false, start)
            }
        }
    }
}
