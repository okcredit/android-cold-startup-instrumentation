package tech.okcredit.startup_instrumentation.internals.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import java.lang.IllegalStateException

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class NextDrawListener(
    private val view: View,
    val onDrawCallback: () -> Unit
) : ViewTreeObserver.OnDrawListener {

    private val handler = Handler(Looper.getMainLooper())
    var invokedInitialOnDraw = false

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onDraw() {
        if (invokedInitialOnDraw) return
        invokedInitialOnDraw = true
        onDrawCallback()
        handler.post {
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnDrawListener(this)
            }
        }
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun View.onNextDraw(onDrawCallback: () -> Unit) {
            if (viewTreeObserver.isAlive && isAttachedToWindow) {
                addNextDrawListener(onDrawCallback)
            } else {
                addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            addNextDrawListener(onDrawCallback)
                            removeOnAttachStateChangeListener(this)
                        }

                        override fun onViewDetachedFromWindow(v: View) = Unit
                    })
            }
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal fun View.addNextDrawListener(callback: () -> Unit) {
            try {
                viewTreeObserver.addOnDrawListener(
                    NextDrawListener(this, callback)
                )
            } catch (e: IllegalStateException) { }
        }
    }
}
