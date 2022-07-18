package `in`.galaxycard.android.utils

import android.Manifest.permission
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.os.Process.myPid
import android.os.Process.myUid
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.*
import android.telephony.CarrierConfigManager
import android.telephony.TelephonyManager
import com.facebook.react.bridge.*
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import io.jsonwebtoken.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class TurboStarterModule(reactContext: ReactApplicationContext?) :
    NativeTurboUtilsSpec(reactContext) {
    override fun initialize() {
        DeviceUtils(reactApplicationContext)

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG)
        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            filter.addAction(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED)
        }
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(TelephonyManager.ACTION_SUBSCRIPTION_CARRIER_IDENTITY_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                sendEvent(
                    reactApplicationContext,
                    "RNDeviceInfo_deviceDataChanged",
                    null
                )
            }
        }
        reactApplicationContext.registerReceiver(receiver, filter)
    }

    override fun getTypedExportedConstants(): MutableMap<String, Any> {
        return DeviceUtils(reactApplicationContext).constants()
    }

    override fun getDeviceData(): WritableNativeMap {
        return Arguments.makeNativeMap(DeviceUtils(reactApplicationContext).dynamicValues())
    }

    private fun getPhoneContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val contactsCursor = reactApplicationContext.contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts._ID + " ASC"
        )
        if (contactsCursor != null && contactsCursor.count > 0) {
            val idIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (contactsCursor.moveToNext()) {
                val id = contactsCursor.getString(idIndex)
                val name = contactsCursor.getString(nameIndex)
                contactsList.add(Contact(id, name))
            }
            contactsCursor.close()
        }
        return contactsList
    }

    private fun getContactNumbers(): HashMap<String, ArrayList<String>> {
        val contactsNumberMap = HashMap<String, ArrayList<String>>()
        val cursor: Cursor? = reactApplicationContext.contentResolver.query(
            Phone.CONTENT_URI,
            null,
            null,
            null,
            Phone.NUMBER + " ASC"
        )
        if (cursor != null && cursor.count > 0) {
            val contactIdIndex = cursor.getColumnIndex(Phone.CONTACT_ID)
            val numberIndex = cursor.getColumnIndex(Phone.NUMBER)
            while (cursor.moveToNext()) {
                val contactId = cursor.getString(contactIdIndex)
                val number: String = cursor.getString(numberIndex)
                //check if the map contains key or not, if not then create a new array list with number
                if (contactsNumberMap.containsKey(contactId)) {
                    contactsNumberMap[contactId]?.add(number)
                } else {
                    contactsNumberMap[contactId] = arrayListOf(number)
                }
            }
            //contact contains all the number of a particular contact
            cursor.close()
        }
        return contactsNumberMap
    }

    private fun getContactEmails(): HashMap<String, ArrayList<String>> {
        val contactsEmailMap = HashMap<String, ArrayList<String>>()
        val cursor: Cursor? = reactApplicationContext.contentResolver.query(
            Email.CONTENT_URI,
            null,
            null,
            null,
            Email.ADDRESS + " ASC"
        )
        if (cursor != null && cursor.count > 0) {
            val contactIdIndex = cursor.getColumnIndex(Email.CONTACT_ID)
            val emailIndex = cursor.getColumnIndex(Email.ADDRESS)
//            val photoIndex = cursor.getColumnIndex(Photo.PHOTO_URI)
            while (cursor.moveToNext()) {
                val contactId = cursor.getString(contactIdIndex)
                val address: String = cursor.getString(emailIndex)
                //check if the map contains key or not, if not then create a new array list with number
                if (contactsEmailMap.containsKey(contactId)) {
                    contactsEmailMap[contactId]?.add(address)
                } else {
                    contactsEmailMap[contactId] = arrayListOf(address)
                }
            }
            //contact contains all the number of a particular contact
            cursor.close()
        }
        return contactsEmailMap
    }

    override fun getContacts(promise: Promise) {
        val context: Context = reactApplicationContext.getBaseContext()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (context.checkPermission(
                    permission.READ_CONTACTS,
                    myPid(),
                    myUid()
                ) != PackageManager.PERMISSION_GRANTED
            )
                return promise.reject(PERMISSION_MISSING, Exception(PERMISSION_MISSING))
        } else if (context.checkSelfPermission(permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return promise.reject(PERMISSION_MISSING, Exception(PERMISSION_MISSING))
        }
        GlobalScope.launch {
            val contactsListAsync = async { getPhoneContacts() }
            val contactNumbersAsync = async { getContactNumbers() }
            val contactEmailAsync = async { getContactEmails() }

            val contacts = contactsListAsync.await()
            val contactNumbers = contactNumbersAsync.await()
            val contactEmails = contactEmailAsync.await()

            val contactsArray = ArrayList<HashMap<String, Any?>>()

            val digest: MessageDigest = MessageDigest.getInstance("MD5")

            contacts.forEach { contact ->
                digest.update(contact.id.toByteArray())
                val map = HashMap<String, Any?>()
                map["name"] = contact.name
                contactNumbers[contact.id]?.let { numbers ->
                    map["phones"] = numbers
                    numbers.forEach {
                        digest.update(it.toByteArray())
                    }
                }
                contactEmails[contact.id]?.let { emails ->
                    map["emails"] = emails
                    emails.forEach {
                        digest.update(it.toByteArray())
                    }
                }
                val contactUri: Uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contact.id.toLong()
                )
                val photoUri: Uri = Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
                map["photo"] = photoUri.toString()

                contactsArray.add(map)
            }

            val messageDigest: ByteArray = digest.digest()
            val hexString = StringBuilder()
            for (byte in messageDigest) {
                val hex = StringBuilder(Integer.toHexString(0xFF and byte.toInt()))
                while (hex.length < 2) hex.insert(0, "0")
                hexString.append(hex)
            }

            val map = HashMap<String, Any>()
            map["contacts"] = contactsArray
            map["hash"] = hexString.toString()

            promise.resolve(Arguments.makeNativeMap(map))
        }
    }

    override fun getInstalledApps(promise: Promise) {
        //get a list of installed apps.
        val packageManager = reactApplicationContext.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val concatenatedNames = StringBuilder()
        val appsList = ArrayList<HashMap<String, Any>>()
        var packageName: String
        for (applicationInfo in packages) {
            if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                val appDetails = HashMap<String, Any>()
                appDetails[PACKAGE] = applicationInfo.packageName
                appDetails[DISPLAY_NAME] = packageManager.getApplicationLabel(applicationInfo).toString()
                appDetails[SYSTEM] = applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM !== 0
                packageName = applicationInfo.packageName
                concatenatedNames.append(packageName)
                val times = installTimeFromPackageManager(packageManager, packageName)
                if (times.isNotEmpty()) {
                    concatenatedNames.append(times[INSTALL])
                    concatenatedNames.append(times[UPDATE])
                    appDetails[INSTALL] = times[INSTALL] as Long
                    appDetails[UPDATE] = times[UPDATE] as Long
                }
                appsList.add(appDetails)
            }
        }
        val hash: String = md5(concatenatedNames.toString())
        val appsWithHash = HashMap<String, Any>()
        appsWithHash[APPS] = appsList
        appsWithHash[HASH] = hash
        promise.resolve(Arguments.makeNativeMap(appsWithHash))
    }

    override fun parseJwt(jws: String, key: String?): WritableNativeMap {
        var jwsString = jws

        val jwsBuilder = Jwts.parserBuilder()

        if (key == null) {
            val i = jws.lastIndexOf('.')
            jwsString = jws.substring(0, i + 1)
        } else {
            val signingKeyResolver = GalaxyCardSigningKeyResolver(key.toByteArray())
            jwsBuilder.setSigningKeyResolver(signingKeyResolver)
        }
        val trusted = jwsBuilder.build()
            .parseClaimsJws(jwsString)

        return Arguments.makeNativeMap(trusted.body);
    }

    private fun installTimeFromPackageManager(
        packageManager: PackageManager,
        packageName: String
    ): HashMap<String, Long> {
        // API level 9 and above have the "firstInstallTime" field.
        // Check for it with reflection and return if present.
        val times = HashMap<String, Long>()
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            var field: Field = PackageInfo::class.java.getField("firstInstallTime")
            times[INSTALL] = field.getLong(info)
            field = PackageInfo::class.java.getField("lastUpdateTime")
            times[UPDATE] = field.getLong(info)
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: IllegalAccessException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalArgumentException) {
        } catch (e: SecurityException) {
        }
        return times
    }

    private fun md5(s: String): String {
        try {
            val digest = MessageDigest.getInstance(HASH_ALGO)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()
            val hexString = java.lang.StringBuilder()
            for (b in messageDigest) {
                hexString.append(Integer.toHexString(0xFF and b.toInt()))
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun sendEvent(
        reactContext: ReactContext,
        eventName: String,
        data: Any?
    ) {
        reactContext
            .getJSModule(RCTDeviceEventEmitter::class.java)
            .emit(eventName, data)
    }

    override fun getName(): String {
        return NAME
    }

//    private external fun nativeMultiply(num1: Double, num2: Double): Double

    companion object {
        const val NAME = "TurboUtils"
        const val HASH_ALGO = "MD5"
        const val PERMISSION_MISSING = "read_contact_permission_missing"
        const val SYSTEM = "system"
        const val INSTALL = "install"
        const val UPDATE = "update"
        const val DISPLAY_NAME = "name"
        const val PACKAGE = "package"
        const val HASH = "hash"
        const val APPS = "apps"

        init {
            System.loadLibrary("reactnativeturboutils-jni")
        }
    }
}
