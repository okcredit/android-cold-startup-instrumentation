package tech.okcredit.startup_instrumentation.internals.app_lifecycle

import android.os.Handler
import android.os.Looper

internal object RecordOfActivityHandlerJobs {
    private val joinedPosts = mutableListOf<() -> Unit>()
    private val handler = Handler(Looper.getMainLooper())

    fun joinPost(post: () -> Unit) {
        val scheduled = joinedPosts.isNotEmpty()
        joinedPosts += post
        if (!scheduled) {
            handler.post {
                for (joinedPost in joinedPosts) {
                    joinedPost()
                }
                joinedPosts.clear()
            }
        }
    }
}
