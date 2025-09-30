package com.jvckenwood.cabmee.homeapp

import android.app.Application
import android.content.Context
import com.jvckenwood.cabmee.homeapp.utilities.LogFileTree
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    // TODO Delete when https://github.com/google/dagger/issues/3601 is resolved.
    @Inject
    @ApplicationContext
    lateinit var context: Context

    // ログを格納するディレクトリ
    private val logDir: String by lazy { getLogDirectory() }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val appName = getString(R.string.app_name)
        Timber.plant(LogFileTree(logDir, appName))
    }

    private fun getLogDirectory(): String {
        val rootPath = filesDir.path
        val dir = File(rootPath, "log")
        if (dir.isDirectory.not()) {
            dir.mkdir()
        }
        return dir.path
    }
}
