package com.jvckenwood.cabmee.homeapp

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class HardwareKeyDetectionService : AccessibilityService() {
    companion object {
        private const val TAG: String = "HWKeyDetection"

        // キーコード
        const val NAVI_KEY = 0x83
        const val A_KEY = 0x84
        const val B_KEY = 0x85
        const val HOME_KEY = 0x86

        // Cabmee車載アプリ
        const val CABMEE_CAR_APP: String = "com.jvckenwood.taitis.taitiscarapp"

        // DiDiパートナーアプリ
        const val DIDI_PARTNER_APP: String = "com.didiglobal.driver"

        // Uberドライバーアプリ
        const val UBER_DRIVER_APP: String = "com.ubercab.driver"
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyEvent(${event?.keyCode})")
        val result = if (event?.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                /* NAVIキー押下*/
                NAVI_KEY -> { onNaviKeyPressed() }
                /* Aキー押下*/
                A_KEY -> { onAKeyPressed() }
                /* Bキー押下*/
                B_KEY -> { onBKeyPressed() }
                /* HOMEキー押下*/
                HOME_KEY -> { onHomeKeyPressed() }
                else -> false
            }
        } else {
            false
        }
        return super.onKeyEvent(event)
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected()")
        //  nop
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt()")
        //  nop
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent()")
        //  nop
    }

    /**
     * NAVI KEY EVENT
     */
    private fun onNaviKeyPressed(): Boolean {
        // Cabmee車載アプリを起動
        return launchApp(CABMEE_CAR_APP)
    }

    /**
     * A KEY EVENT
     */
    private fun onAKeyPressed(): Boolean {
        // 3rdパーティアプリを起動
        return if (checkInstalled(DIDI_PARTNER_APP)) {
            // DiDiパートナーアプリを起動
            launchApp(DIDI_PARTNER_APP)
        } else if (checkInstalled(UBER_DRIVER_APP)) {
            // Uberドライバーアプリを起動
            launchApp(UBER_DRIVER_APP)
        } else {
            false
        }
    }

    /**
     * B KEY EVENT
     */
    private fun onBKeyPressed(): Boolean {
        return false
    }

    /**
     * HOME KEY EVENT
     */
    private fun onHomeKeyPressed(): Boolean {
        return false
    }

    /**
     * アプリケーションを起動する
     */
    private fun launchApp(app: String): Boolean {
        Log.d(TAG, "launchApp($app)")
        try {
            // Activity Managerを取得
            val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            // 対象のアプリ内で最前面にあるActivity名を取得する
            var topActivityName: String? = null
            var displaying = false
            val runningTasks: List<ActivityManager.RunningTaskInfo> = am.getRunningTasks(Int.MAX_VALUE)
            for (i in runningTasks.indices) {
                val task = runningTasks[i]
                val packageName = task.baseActivity?.packageName
                val activityName = task.topActivity?.getClassName()
                if ((topActivityName == null) and packageName.equals(app)) {
                    displaying = (i == 0)
                    topActivityName = activityName
                    break
                }
            }

            // 既にアプリが前面に表示されている場合は処理しない
            if (displaying.not()) {
                val pm: PackageManager = this.packageManager
                if (topActivityName != null) {
                    // 対象のアプリケーションが既に起動中の場合は、Activityを最前面に移動
                    val intent = Intent()
                    intent.setClassName(app, topActivityName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    this.startActivity(intent)
                } else {
                    // アプリケーションが起動していない場合は、再起動する
                    val intent = pm.getLaunchIntentForPackage(app)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    this.startActivity(intent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * アプリケーションはインストール済みか？
     */
    private fun checkInstalled(app: String): Boolean {
        // Package Managerを取得
        val pm: PackageManager = this.packageManager
        // インストール済みアプリ一覧を取得
        val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.MATCH_DISABLED_COMPONENTS
        val installedApps = pm.getInstalledApplications(flags).map { it.packageName }
        // インストール済みか？
        return app in installedApps
    }

    /**
     * フラグメントの表示順番をログに表示する
     */
    private fun showFragmentOrder() {
        try {
            // Activity Managerを取得
            val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            var cabmeeTop = false
            var didiTop = false
            val runningTasks: List<ActivityManager.RunningTaskInfo> = am.getRunningTasks(Int.MAX_VALUE)
            Log.d(TAG, "-------------------------------------")
            for (i in runningTasks.indices) {
                val task = runningTasks[i]
                val packageName = task.baseActivity?.packageName
                val activityName = task.topActivity?.getClassName()
                if (packageName == CABMEE_CAR_APP) {
                    if (cabmeeTop.not()) {
                        Log.d(TAG, "[C] ${i + 1}. PACKAGE NAME $packageName ($activityName)")
                        cabmeeTop = true
                    } else {
                        Log.d(TAG, "(c) ${i + 1}. PACKAGE NAME $packageName ($activityName)")
                    }
                } else if (packageName == DIDI_PARTNER_APP) {
                    if (didiTop.not()) {
                        Log.d(TAG, "[D] ${i + 1}. PACKAGE NAME $packageName ($activityName)")
                        didiTop = true
                    } else {
                        Log.d(TAG, "(d) ${i + 1}. PACKAGE NAME $packageName ($activityName)")
                    }
                } else {
                    Log.d(TAG, "    ${i + 1}. PACKAGE NAME $packageName ($activityName)")
                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}
