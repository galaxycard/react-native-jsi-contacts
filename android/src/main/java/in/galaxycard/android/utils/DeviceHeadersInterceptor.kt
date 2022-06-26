package `in`.galaxycard.android.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.BatteryState
import android.os.Build
import android.util.Base64
import androidx.annotation.NonNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class DeviceHeadersInterceptor(private val context: Context): Interceptor {
    private var mSignatureHash: String? = null

    init {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
            )
            for(signature in if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.signingInfo.apkContentsSigners else packageInfo.signatures) {
                try {
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    mSignatureHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                    break
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    @NonNull
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        if (mSignatureHash != null) {
            builder.addHeader("x-gct-signature", mSignatureHash!!)
        }
        val constants = DeviceUtils(context).constants()
        builder.addHeader("device", constants["deviceId"]!!)
        builder.addHeader("build", constants["buildNumber"]!!)
        builder.addHeader("os-version", constants["systemVersion"]!!)
        builder.addHeader("brand", constants["brand"]!!)
        builder.addHeader("model", constants["model"]!!)
        builder.addHeader("install-referrer", constants["installReferrer"]!!)

        val deviceData = DeviceUtils(context).dynamicValues()
        builder.addHeader("carrier", deviceData["carrier"] as String)
        val headphones = if (deviceData["hasHeadphones"] as Boolean) "yes" else "no"
        builder.addHeader("headphones-connected", headphones)
        builder.addHeader("airplane-mode", deviceData["airplaneMode"] as String)
        val airplaneMode = if (deviceData["airplaneMode"] as Boolean) "on" else "off"
        builder.addHeader("airplane-mode", airplaneMode)
        builder.addHeader("battery-level", deviceData[DeviceUtils.BATTERY_LEVEL] as String)
        builder.addHeader("battery-state", deviceData[DeviceUtils.BATTERY_STATE] as String)
        builder.addHeader("low-power-mode", deviceData[DeviceUtils.LOW_POWER_MODE] as String)
        builder.addHeader("font-scale", deviceData["fontScale"] as String)
        builder.addHeader("disk-total", deviceData["totalDiskCapacity"] as String)
        builder.addHeader("disk-available", deviceData["freeDiskStorage"] as String)
        builder.addHeader("memory-total", deviceData["maxMemory"] as String)
        builder.addHeader("memory-used", deviceData["usedMemory"] as String)
        builder.addHeader("pin-or-fingerprint-set", deviceData["pinOrFingerprintSet"] as String)
        val locationEnabled = if (deviceData["hasLocation"] as Boolean) "yes" else "no"
        builder.addHeader("location-enabled", deviceData["locationEnabled"] as String)

        return chain.proceed(builder.build())
    }
}