package `in`.galaxycard.android.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import androidx.annotation.NonNull
import okhttp3.Headers
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
        val headers = Headers.Builder()
        if (mSignatureHash != null) {
            headers.add("x-gct-signature", mSignatureHash!!)
        }
        val constants = DeviceUtils(context).constants()
        if (chain.request().header("device") == null) {
            headers.add("device", constants["uniqueId"]!! as String)
        } else {
            headers.add("device", chain.request().header("device")!!)
        }
        headers.add("build", constants["buildNumber"]!! as String)
        headers.add("build-number", constants["buildNumber"]!! as String)
        headers.add("app-version", constants["appVersion"]!! as String)
        headers.add("os-version", constants["systemVersion"]!! as String)
        headers.add("device-code", constants["deviceId"]!! as String)
        headers.add("brand", constants["brand"]!! as String)
        headers.add("model", constants["model"]!! as String)
        headers.addUnsafeNonAscii("install-referrer", constants["installReferrer"]!! as String)
        headers.addUnsafeNonAscii("x-gct-install-referrer", constants["installReferrer"]!! as String)
        headers.add("screen-width", constants["screenWidth"]!!.toString())
        headers.add("screen-height", constants["screenHeight"]!!.toString())
        headers.add("screen-density", constants["screenDensity"]!!.toString())

        val deviceData = DeviceUtils(context).dynamicValues()
        headers.addUnsafeNonAscii("carrier", deviceData["carrier"] as String)
        val headphones = if (deviceData["hasHeadphones"] as Boolean) "yes" else "no"
        headers.add("headphones-connected", headphones)
        val airplaneMode = if (deviceData["airplaneMode"] as Boolean) "on" else "off"
        headers.add("airplane-mode", airplaneMode)
        headers.add("battery-level", deviceData[DeviceUtils.BATTERY_LEVEL].toString())
        headers.add("battery-state", deviceData[DeviceUtils.BATTERY_STATE] as String)
        val lowPowerMode = if (deviceData[DeviceUtils.LOW_POWER_MODE] as Boolean) "on" else "off"
        headers.add("low-power-mode", lowPowerMode)
        headers.add("font-scale", deviceData["fontScale"].toString())
        headers.add("disk-total", (deviceData["totalDiskCapacity"] as BigDecimal).toPlainString())
        headers.add("disk-available", (deviceData["freeDiskStorage"] as BigDecimal).toPlainString())
        headers.add("memory-total", (deviceData["maxMemory"] as BigDecimal).toPlainString())
        headers.add("memory-used", (deviceData["usedMemory"] as BigDecimal).toPlainString())
        val pinOrFingerprintSet = if (deviceData["pinOrFingerprintSet"] as Boolean) "yes" else "no"
        headers.add("pin-or-fingerprint-set", pinOrFingerprintSet)
        val locationEnabled = if (deviceData["hasLocation"] as Boolean) "yes" else "no"
        headers.add("location-enabled", locationEnabled)
        headers.addUnsafeNonAscii("wifi-name", deviceData["wifiName"] as String)
        headers.addUnsafeNonAscii("access-point-name", deviceData["accessPointName"] as String)
        headers.addUnsafeNonAscii("device-name", deviceData["deviceName"] as String)

        return chain.proceed(builder.build())
    }
}