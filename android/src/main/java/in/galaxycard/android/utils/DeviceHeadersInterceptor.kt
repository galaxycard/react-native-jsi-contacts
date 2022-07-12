package `in`.galaxycard.android.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import androidx.annotation.NonNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.math.BigDecimal
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
        builder.addHeader("device", constants["uniqueId"]!! as String)
        builder.addHeader("build", constants["buildNumber"]!! as String)
        builder.addHeader("os-version", constants["systemVersion"]!! as String)
        builder.addHeader("device-code", constants["deviceId"]!! as String)
        builder.addHeader("brand", constants["brand"]!! as String)
        builder.addHeader("model", constants["model"]!! as String)
        builder.addHeader("install-referrer", constants["installReferrer"]!! as String)
        builder.addHeader("screen-width", constants["screenWidth"]!!.toString())
        builder.addHeader("screen-height", constants["screenHeight"]!!.toString())
        builder.addHeader("screen-density", constants["screenDensity"]!!.toString())

        val deviceData = DeviceUtils(context).dynamicValues()
        builder.addHeader("carrier", deviceData["carrier"] as String)
        val headphones = if (deviceData["hasHeadphones"] as Boolean) "yes" else "no"
        builder.addHeader("headphones-connected", headphones)
        val airplaneMode = if (deviceData["airplaneMode"] as Boolean) "on" else "off"
        builder.addHeader("airplane-mode", airplaneMode)
        builder.addHeader("battery-level", deviceData[DeviceUtils.BATTERY_LEVEL].toString())
        builder.addHeader("battery-state", deviceData[DeviceUtils.BATTERY_STATE] as String)
        val lowPowerMode = if (deviceData[DeviceUtils.LOW_POWER_MODE] as Boolean) "on" else "off"
        builder.addHeader("low-power-mode", lowPowerMode)
        builder.addHeader("font-scale", deviceData["fontScale"].toString())
        builder.addHeader("disk-total", (deviceData["totalDiskCapacity"] as BigDecimal).toPlainString())
        builder.addHeader("disk-available", (deviceData["freeDiskStorage"] as BigDecimal).toPlainString())
        builder.addHeader("memory-total", (deviceData["maxMemory"] as BigDecimal).toPlainString())
        builder.addHeader("memory-used", (deviceData["usedMemory"] as BigDecimal).toPlainString())
        val pinOrFingerprintSet = if (deviceData["pinOrFingerprintSet"] as Boolean) "yes" else "no"
        builder.addHeader("pin-or-fingerprint-set", pinOrFingerprintSet)
        val locationEnabled = if (deviceData["hasLocation"] as Boolean) "yes" else "no"
        builder.addHeader("location-enabled", locationEnabled)
        builder.addHeader("wifi-name", deviceData["wifiName"] as String)
        builder.addHeader("access-point-name", deviceData["accessPointName"] as String)
        builder.addHeader("device-name", deviceData["deviceName"] as String)

        return chain.proceed(builder.build())
    }
}