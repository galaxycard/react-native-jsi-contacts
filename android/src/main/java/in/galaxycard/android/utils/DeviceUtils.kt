package `in`.galaxycard.android.utils

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.media.AudioManager
import android.os.*
import androidx.core.content.pm.PackageInfoCompat
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.telephony.TelephonyManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import java.math.BigInteger
import com.sun.scenario.Settings

class DeviceUtils(private val context: Context) {
    companion object {
        const val BATTERY_STATE = "batteryState"
        const val BATTERY_LEVEL = "batteryLevel"
        const val INSTALL_REFERRER = "installReferrer"
        const val LOW_POWER_MODE = "lowPowerMode"
    }

    init {
        val sharedPreferences = context.getSharedPreferences(TurboStarterModule.NAME, Context.MODE_PRIVATE)

        if (!sharedPreferences.contains(INSTALL_REFERRER)) {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()

            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString(INSTALL_REFERRER, referrerClient.installReferrer.installReferrer)
                            editor.apply()
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }

    fun constants(): MutableMap<String, String> {
        var appVersion: String
        var buildNumber: String
        var appName: String
        try {
            val packageInfo = getPackageInfo(context)
            appVersion = packageInfo.versionName
            buildNumber = PackageInfoCompat.getLongVersionCode(packageInfo).toString()
            appName =
                context.applicationInfo.loadLabel(context.packageManager)
                    .toString()
        } catch (e: Exception) {
            appVersion = Build.UNKNOWN
            buildNumber = Build.UNKNOWN
            appName = Build.UNKNOWN
        }
        val constants: MutableMap<String, String> = HashMap()
        constants["uniqueId"] =
            getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        constants["deviceId"] = Build.BOARD
        constants["bundleId"] = context.packageName
        constants["systemVersion"] = Build.VERSION.RELEASE
        constants["appVersion"] = appVersion
        constants["buildNumber"] = buildNumber
        constants["appName"] = appName
        constants["brand"] = Build.BRAND
        constants["model"] = Build.MODEL
        val sharedPref = context.getSharedPreferences(
            TurboStarterModule.NAME,
            Context.MODE_PRIVATE
        )
        constants["installReferrer"] = sharedPref.getString(INSTALL_REFERRER, Build.UNKNOWN)!!

        return constants
    }

    fun dynamicValues(): HashMap<String, Any> {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val deviceData = HashMap<String, Any>()

        deviceData["hasHeadphones"] = audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn

        val telMgr =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        deviceData["carrier"] = telMgr.networkOperatorName

        deviceData["airplaneMode"] = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0

        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val powerState = getPowerStateFromIntent(context, intent)

        if (powerState != null) {
            deviceData[BATTERY_STATE] = powerState[BATTERY_STATE]!!
            deviceData[BATTERY_LEVEL] = powerState[BATTERY_LEVEL]!!
            deviceData[LOW_POWER_MODE] = powerState[LOW_POWER_MODE]!!
        }
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        deviceData["pinOrFingerprintSet"] = keyguardManager.isKeyguardSecure

        deviceData["fontScale"] = context.resources.configuration.fontScale

        try {
            val rootDir = StatFs(Environment.getRootDirectory().absolutePath)
            val dataDir = StatFs(Environment.getDataDirectory().absolutePath)
            val rootFree: Double =
                BigInteger.valueOf(rootDir.availableBlocksLong).multiply(BigInteger.valueOf(rootDir.blockSizeLong)).toDouble()
            val dataFree: Double =
                BigInteger.valueOf(dataDir.availableBlocksLong).multiply(BigInteger.valueOf(rootDir.blockSizeLong)).toDouble()
            deviceData["freeDiskStorage"] = rootFree + dataFree
        } catch (e: java.lang.Exception) {
            deviceData["freeDiskStorage"] = -1
        }

        try {
            val rootDir = StatFs(Environment.getRootDirectory().absolutePath)
            val dataDir = StatFs(Environment.getDataDirectory().absolutePath)

            val rootDirCapacity: BigInteger = BigInteger.valueOf(rootDir.blockCountLong).multiply(
                BigInteger.valueOf(rootDir.blockSizeLong))
            val dataDirCapacity: BigInteger = BigInteger.valueOf(dataDir.blockCountLong).multiply(
                BigInteger.valueOf(dataDir.blockSizeLong))
            deviceData["totalDiskCapacity"] = rootDirCapacity.add(dataDirCapacity).toDouble()
        } catch (e: java.lang.Exception) {
            deviceData["totalDiskCapacity"] = -1
        }

        deviceData["maxMemory"] = Runtime.getRuntime().maxMemory().toDouble()

        val actMgr =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pid = Process.myPid()
        val memInfos = actMgr.getProcessMemoryInfo(intArrayOf(pid))
        if (memInfos.size != 1) {
            System.err.println("Unable to getProcessMemoryInfo. getProcessMemoryInfo did not return any info for the PID")
            deviceData["usedMemory"] = -1
        } else {
            val memInfo = memInfos[0]
            deviceData["usedMemory"] = memInfo.totalPss * 1024.0
        }

        var hasLocation = false
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        hasLocation =
            hasLocation or locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasLocation =
            hasLocation or locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        deviceData["hasLocation"] = hasLocation

        return deviceData
    }

    @Throws(java.lang.Exception::class)
    private fun getPackageInfo(context: Context): PackageInfo {
        return context.packageManager.getPackageInfo(
            context.packageName,
            0
        )
    }

    private fun getPowerStateFromIntent(context: Context, intent: Intent?): HashMap<String, Any>? {
        if (intent == null) {
            return null
        }
        val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val isPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val batteryPercentage = batteryLevel / batteryScale.toFloat()
        var batteryState = Build.UNKNOWN
        if (isPlugged == 0) {
            batteryState = "unplugged"
        } else if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            batteryState = "charging"
        } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
            batteryState = "full"
        }
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var powerSaveMode = powerManager.isPowerSaveMode
        val powerState = HashMap<String, Any>()
        powerState[BATTERY_STATE] = batteryState
        powerState[BATTERY_LEVEL] = batteryPercentage.toDouble()
        powerState[LOW_POWER_MODE] = powerSaveMode
        return powerState
    }
}