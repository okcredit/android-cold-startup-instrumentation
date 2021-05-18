package tech.okcredit.startup_instrumentation.internals

import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.TimeUnit

object AppStartUpMeasurementUtils {

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

    private fun readProcessStartTicks(pid: Int): Long {
        val path = "/proc/$pid/stat"
        val stat = BufferedReader(FileReader(path)).use { reader ->
            reader.readLine()
        }
        val fields = stat.substringAfter(") ")
            .split(' ')
        return fields[19].toLong()
    }
}
