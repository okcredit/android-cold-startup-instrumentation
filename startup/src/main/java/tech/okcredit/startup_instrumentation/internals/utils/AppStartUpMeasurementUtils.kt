package tech.okcredit.startup_instrumentation.internals.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal object AppStartUpMeasurementUtils {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getProcessForkTime(): Long {
        val forkRealtime = readProcessForkRealtimeMillis()
        val nowRealtime = SystemClock.elapsedRealtime()
        val nowUptime = SystemClock.uptimeMillis()
        val elapsedRealtime = nowRealtime - forkRealtime

        return nowUptime - elapsedRealtime
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun readProcessForkRealtimeMillis(): Long {
        val myPid = Process.myPid()
        val ticksAtProcessStart = readProcessStartTicks(myPid)
        val ticksPerSecond = Os.sysconf(OsConstants._SC_CLK_TCK)
        return TimeUnit.SECONDS.toMillis(ticksAtProcessStart) / ticksPerSecond
    }

    /*** On Linux & Android, there's a file called /proc/[pid]/stat that is readable and contains
     stats for each process, including the process start time.
     /proc/[pid]/stat is a file with one line of text, where each stat is separated by a space.
     However, the second entry is the filename of the executable, which may contain spaces,
     so we'll have to jump past it by looking for the first ) character. Once we've done that,
     we can split the remaining string by spaces and pick the 20th entry at index 19. ****/
    private fun readProcessStartTicks(pid: Int): Long {
        val path = "/proc/$pid/stat"
        val stat = BufferedReader(FileReader(path)).use { reader ->
            reader.readLine()
        }
        val fields = stat.substringAfter(") ")
            .split(' ')
        return fields[19].toLong()
    }

    fun getSingleThreadExecutorForLaunchTracker(): ExecutorService {
        return Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable).apply {
                name = "app-launch-tracker-executor"
            }
        }
    }

    fun Context.getProcessInfo(): ActivityManager.RunningAppProcessInfo? {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        var processInfo: ActivityManager.RunningAppProcessInfo? = null

        activityManager.runningAppProcesses?.let { runningProcesses ->
            for (process in runningProcesses) {
                if (process.pid == Process.myPid()) {
                    processInfo = process
                }
            }
        }

        return processInfo
    }
}
