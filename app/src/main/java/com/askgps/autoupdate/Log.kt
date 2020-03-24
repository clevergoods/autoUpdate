package com.askgps.autoupdate

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.core.os.bundleOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "pTracker"

fun Any?.toLog(prefix: String = "", methodName: String? = null, tag: String = TAG) {
    var tempMethodName = methodName
    try {
        tempMethodName = methodName ?: Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception){}
    Log.d(tag,
        "[${String.format("%5d", Thread.currentThread().id)}] [${String.format("%-15s", tempMethodName)}] $prefix: ${this}")
}

fun Any?.toFile(prefix: String = "", methodName: String? = null, tag: String = TAG) {
    var tempMethodName = methodName
    try {
        tempMethodName = methodName ?: Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception){}
    val str = when (this) {
        is Array<*> -> this.joinToString("\n")
        is Iterable<*> -> this.joinToString("\n")
        is Throwable -> this.stackTrace.joinToString("\n")
        else -> this
    }
    str.toLog(prefix, tempMethodName, tag)
    Logger.writeToFile(str, prefix, tempMethodName, Thread.currentThread().id)
}

fun Throwable.toLog(prefix: String = "", methodName: String? = null, tag: String = TAG) {
    var tempMethodName = methodName
    try {
        tempMethodName = methodName ?: Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception){}
    this.message.toLog(prefix, tempMethodName, tag)
    this.printStackTrace()
}

fun <T> Iterable<T>.toLog(prefix: String = "", methodName: String? = null, tag: String = TAG) {
    var tempMethodName = methodName
    try {
        tempMethodName = methodName ?: Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception){}
    this.joinToString().toLog(prefix, tempMethodName, tag)
}

fun <T> Iterable<T>.toLogLn(prefix: String = "", methodName: String? = null, tag: String = TAG) {
    var tempMethodName = methodName
    try {
        tempMethodName = methodName ?: Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception){}
    "\n${this.joinToString("\n")}".toLog(prefix, tempMethodName, tag)
}

object Logger {

    private val date = SimpleDateFormat("ddMMyy").format(Date(System.currentTimeMillis()))
    private const val TO_FILE = 0
    private lateinit var file: File
    private val FILE_NAME = "tracker_$date.txt"

    private const val METHOD_NAME = "methodName"
    private const val PREFIX = "prefix"
    private const val THREAD_ID = "threadId"

    fun initFile(context: Context) {
        val path = File(context.getExternalFilesDir(
            Environment.DIRECTORY_DOCUMENTS), "Log")
        path.mkdir()
        file = File(path, FILE_NAME)
        file.takeIf { !it.exists() }?.createNewFile()
    }

    private val handlerThread = HandlerThread("logger").also {
        it.isDaemon = true
        it.start()
    }

    private val handler: Handler by lazy {
        Handler(handlerThread.looper) {msg ->
            when(msg.what) {
                TO_FILE -> {
                    toFile(msg.obj, msg.data.getString(PREFIX), msg.data.getString(METHOD_NAME), msg.data.getLong(
                        THREAD_ID))
                    return@Handler true
                }
            }
            return@Handler false
        }
    }

    fun writeToFile(obj: Any?, prefix: String? = null, methodName: String? = null, threadId: Long? = null) {
        handler.sendMessage(Message().also {
            it.what = TO_FILE
            it.obj = obj
            it.data = bundleOf(PREFIX to (prefix ?: ""), METHOD_NAME to (methodName ?: ""), THREAD_ID to threadId )
        })
    }

    private fun toFile(msg: Any?, prefix: String? = null, methodName: String? = null, threadId: Long? = null) {
        try {
            file.appendText(
                "[${SimpleDateFormat("ddMMyy HH:mm:ss").format(Calendar.getInstance().time)}] [${String.format(
                    "%5d",
                    threadId ?: Thread.currentThread().id
                )}]" +
                        "[${String.format("%-15s", methodName)}] $prefix: $msg \n"
            )
        } catch (ex: Exception) {

        }
    }
}
