package tech.okcredit.startup_instrumentation.internals.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.lang.RuntimeException

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
        try {
            onDrawCallback()
        } catch (e: NoSuchElementException) {
            // The callback being requested does not exist on window
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
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
            viewTreeObserver.addOnDrawListener(
                NextDrawListener(this, callback)
            )
        }
    }
}
