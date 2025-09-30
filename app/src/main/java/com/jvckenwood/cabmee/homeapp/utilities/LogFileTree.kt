package com.jvckenwood.cabmee.homeapp.utilities

import android.util.Log
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class LogFileTree constructor(private val logDir: String, private val filename: String) : Timber.Tree() {
    companion object {
        const val FILE_SIZE_LIMIT: Int = 2 * 1024 * 1024
        const val MAX_NUM_OF_FILES: Int = 10
    }

    private val logger by lazy {
        Logger.getLogger("LogFileTree").also {
            it.useParentHandlers = false
            it.level = Level.INFO
            if (it.handlers.isEmpty()) {
                val logFile = "$logDir/$filename.log.%g"
                val fileHandler = FileHandler(logFile, FILE_SIZE_LIMIT, MAX_NUM_OF_FILES, true)
                fileHandler.formatter = object : Formatter() {
                    override fun format(record: LogRecord): String = record.message
                }
                it.addHandler(fileHandler)
            }
        }
    }

    @Override
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = priority2Level(priority)
        val msg = format(priority, tag, message)
        if (t == null) {
            logger.log(level, msg)
        } else {
            logger.log(level, msg, t)
        }
    }

    private fun priority2Level(priority: Int): Level =
        when (priority) {
            Log.VERBOSE -> Level.FINER
            Log.DEBUG -> Level.FINE
            Log.INFO -> Level.INFO
            Log.WARN -> Level.WARNING
            Log.ERROR -> Level.SEVERE
            Log.ASSERT -> Level.SEVERE
            else -> Level.FINEST
        }

    private fun format(priority: Int, tag: String?, message: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        val datetimeString = LocalDateTime.now(ZoneId.of("Asia/Tokyo")).format(formatter)
        val prefixString = arrayOf("?/", "?/", "V/", "D/", "I/", "W/", "E/", "WTF/")[priority]
        val tagString = tag ?: ""
        return "$datetimeString $prefixString$tagString $message\n"
    }
}
