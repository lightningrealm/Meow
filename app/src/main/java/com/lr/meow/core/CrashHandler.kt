package com.lr.meow.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        handleException(e)
        
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e)
        } else {
            exitProcess(1)
        }
    }

    private fun handleException(e: Throwable) {
        try {
            val logDir = context.getExternalFilesDir("crash_logs")
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "crash_$timestamp.log"
            val file = File(logDir, fileName)

            val writer = FileWriter(file)
            val printWriter = PrintWriter(writer)

            printWriter.println("=== Device Info ===")
            printWriter.println("OS Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            printWriter.println("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            
            try {
                val pi = context.packageManager.getPackageInfo(context.packageName, 0)
                printWriter.println("App Version: ${pi.versionName} (${pi.versionCode})")
            } catch (ignored: PackageManager.NameNotFoundException) {}

            printWriter.println()
            printWriter.println("=== Exception Trace ===")
            
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            printWriter.print(sw.toString())

            printWriter.close()
            writer.close()
        } catch (ex: Exception) {
            // Ignore exceptions during crash logging
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: CrashHandler? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = CrashHandler(context.applicationContext)
                    }
                }
            }
        }
    }
}
