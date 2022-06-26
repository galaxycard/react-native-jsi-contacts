package `in`.galaxycard.android.utils

import android.content.*
import android.database.Cursor
import android.media.AudioManager
import android.os.*
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.*
import android.provider.Settings.Secure.getString
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter


class TurboStarterModule(reactContext: ReactApplicationContext?) :
    NativeTurboStarterSpec(reactContext), LifecycleEventListener {

    val ID_FOR_PROFILE_CONTACT = -1

    private val FULL_PROJECTION = arrayOf(
        ContactsContract.Data._ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.RAW_CONTACT_ID,
        ContactsContract.Data.LOOKUP_KEY,
        ContactsContract.Contacts.Data.MIMETYPE,
        ContactsContract.Profile.DISPLAY_NAME,
        ContactsContract.Data.PHOTO_URI,
        StructuredName.DISPLAY_NAME,
        StructuredName.GIVEN_NAME,
        StructuredName.MIDDLE_NAME,
        StructuredName.FAMILY_NAME,
        StructuredName.PREFIX,
        StructuredName.SUFFIX,
        Phone.NUMBER,
        Phone.NORMALIZED_NUMBER,
        Phone.TYPE,
        Phone.LABEL,
        Email.DATA,
        Email.ADDRESS,
        Email.TYPE,
        Email.LABEL,
        Organization.COMPANY,
        Organization.TITLE,
        Organization.DEPARTMENT,
        StructuredPostal.FORMATTED_ADDRESS,
        StructuredPostal.TYPE,
        StructuredPostal.LABEL,
        StructuredPostal.STREET,
        StructuredPostal.POBOX,
        StructuredPostal.NEIGHBORHOOD,
        StructuredPostal.CITY,
        StructuredPostal.REGION,
        StructuredPostal.POSTCODE,
        StructuredPostal.COUNTRY,
        Note.NOTE,
        Website.URL,
        Im.DATA,
        Event.START_DATE,
        Event.TYPE,
    )

    init {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG)
        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
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

        DeviceUtils(reactApplicationContext)
    }

    override fun getConstants(): Map<String, Any> {
        return DeviceUtils(reactApplicationContext).constants()
    }

    override fun getDeviceData(): WritableNativeMap {
        return Arguments.makeNativeMap(DeviceUtils(reactApplicationContext).dynamicValues())
    }

    override fun getContacts(promise: Promise) {
        val cursor = reactApplicationContext.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            FULL_PROJECTION,
            ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                Email.CONTENT_ITEM_TYPE,
                Phone.CONTENT_ITEM_TYPE,
                StructuredName.CONTENT_ITEM_TYPE,
                Organization.CONTENT_ITEM_TYPE,
                StructuredPostal.CONTENT_ITEM_TYPE,
                Note.CONTENT_ITEM_TYPE,
                Website.CONTENT_ITEM_TYPE,
                Im.CONTENT_ITEM_TYPE,
                Event.CONTENT_ITEM_TYPE,
            ),
            null
        )
        val map: HashMap<String, HashMap<String, Any?>> = HashMap()

        while (cursor != null && cursor.moveToNext()) {
            val columnIndexContactId: Int = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
            val columnIndexId: Int = cursor.getColumnIndex(ContactsContract.Data._ID)
            val columnIndexRawContactId: Int =
                cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)
            var contactId: String
            var id: String?
            var rawContactId: String
            if (columnIndexContactId != -1) {
                contactId = cursor.getString(columnIndexContactId)
            } else {
                //todo - double check this, it may not be necessary any more
                contactId =
                    java.lang.String.valueOf(ID_FOR_PROFILE_CONTACT) //no contact id for 'ME' user
            }
            if (columnIndexId != -1) {
                id = cursor.getString(columnIndexId)
            } else {
                //todo - double check this, it may not be necessary any more
                id = java.lang.String.valueOf(ID_FOR_PROFILE_CONTACT) //no contact id for 'ME' user
            }
            if (columnIndexRawContactId != -1) {
                rawContactId = cursor.getString(columnIndexRawContactId)
            } else {
                //todo - double check this, it may not be necessary any more
                rawContactId =
                    java.lang.String.valueOf(ID_FOR_PROFILE_CONTACT) //no contact id for 'ME' user
            }
            if (!map.containsKey(contactId)) {
                val contact = object : HashMap<String, Any?>() {
                    init {
                        put("contactId", contactId)
                        put("phones", ArrayList<Map<String, String>>())
                        put("emails", ArrayList<Map<String, String>>())
                        put("postalAddresses", ArrayList<Map<String, String>>())
                        put("instantMessengers", ArrayList<Map<String, String>>())
                    }
                }
                map[contactId] = contact
            }
            val contact: HashMap<String, Any?>? = map[contactId]
            val mimeType: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))
            val name: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            contact?.put("rawContactId", rawContactId)
            if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(contact?.get("displayName") as String?)) {
                contact?.put("displayName", name)
            }
            if (TextUtils.isEmpty(contact?.get("photoUri") as String?)) {
                if (cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI) != -1) {
                    val rawPhotoURI: String =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI))
                    if (!TextUtils.isEmpty(rawPhotoURI)) {
                        contact?.put("photoUri", rawPhotoURI)
                        contact?.put("hasPhoto", true)
                    }
                }
            }
            when (mimeType) {
                StructuredName.CONTENT_ITEM_TYPE -> {
                    contact?.put("givenName", cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME)))
                    if (cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME)) != null) {
                        contact?.put("middleName", cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME)))
                    } else {
                        contact?.put("middleName", "")
                    }
                    if (cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME)) != null) {
                        contact?.put("familyName", cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME)))
                    } else {
                        contact?.put("familyName", "")
                    }
                    contact?.put("prefix", cursor.getString(cursor.getColumnIndex(StructuredName.PREFIX)))
                    contact?.put("suffix", cursor.getString(cursor.getColumnIndex(StructuredName.SUFFIX)))
                }

                Phone.CONTENT_ITEM_TYPE -> {
                    val phoneNumber: String = cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                    val phoneType: Int = cursor.getInt(cursor.getColumnIndex(Phone.TYPE))
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        var label = when (phoneType) {
                            Phone.TYPE_HOME -> "home"
                            Phone.TYPE_WORK -> "work"
                            Phone.TYPE_MOBILE -> "mobile"
                            Phone.TYPE_OTHER -> "other"
                            else -> "other"
                        }
                        (contact?.get("phones") as ArrayList<HashMap<String, String>>).add(object : HashMap<String, String>() {
                            init {
                                put(label, phoneNumber)
                            }
                        })
                    }
                }
                Email.CONTENT_ITEM_TYPE -> {
                    val email: String = cursor.getString(cursor.getColumnIndex(Email.ADDRESS))
                    val emailType: Int = cursor.getInt(cursor.getColumnIndex(Email.TYPE))
                    if (!TextUtils.isEmpty(email)) {
                        var label = when (emailType) {
                            Email.TYPE_HOME -> "home"
                            Email.TYPE_WORK -> "work"
                            Email.TYPE_MOBILE -> "mobile"
                            Email.TYPE_OTHER -> "other"
                            Email.TYPE_CUSTOM -> if (cursor.getString(cursor.getColumnIndex(Email.LABEL)) != null) {
                                cursor.getString(cursor.getColumnIndex(Email.LABEL)).toLowerCase()
                            } else {
                                ""
                            }
                            else -> "other"
                        }
                        (contact?.get("emails") as ArrayList<HashMap<String, String>>).add(object : HashMap<String, String>() {
                            init {
                                put(label, email)
                            }
                        })
                    }
                }
                Website.CONTENT_ITEM_TYPE -> {
                    val url: String = cursor.getString(cursor.getColumnIndex(Website.URL))
                    val websiteType: Int = cursor.getInt(cursor.getColumnIndex(Website.TYPE))
                    if (!TextUtils.isEmpty(url)) {
                        var label = when (websiteType) {
                            Website.TYPE_HOMEPAGE -> "homepage"
                            Website.TYPE_BLOG -> "blog"
                            Website.TYPE_PROFILE -> "profile"
                            Website.TYPE_HOME -> "home"
                            Website.TYPE_WORK -> "work"
                            Website.TYPE_FTP -> "ftp"
                            Website.TYPE_CUSTOM -> if (cursor.getString(
                                    cursor.getColumnIndex(
                                        Website.LABEL
                                    )
                                ) != null
                            ) {
                                cursor.getString(cursor.getColumnIndex(Website.LABEL)).toLowerCase()
                            } else {
                                ""
                            }
                            else -> "other"
                        }
                        (contact?.get("urls") as ArrayList<HashMap<String, String>>).add(object : HashMap<String, String>() {
                            init {
                                put(label, url)
                            }
                        })
                    }
                }
                Im.CONTENT_ITEM_TYPE -> {
                    val username: String = cursor.getString(cursor.getColumnIndex(Im.DATA))
                    val imType: Int = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL))
                    if (!TextUtils.isEmpty(username)) {
                        var label = when (imType) {
                            Im.PROTOCOL_AIM -> "AIM"
                            Im.PROTOCOL_MSN -> "MSN"
                            Im.PROTOCOL_YAHOO -> "Yahoo"
                            Im.PROTOCOL_SKYPE -> "Skype"
                            Im.PROTOCOL_QQ -> "QQ"
                            Im.PROTOCOL_GOOGLE_TALK -> "Google Talk"
                            Im.PROTOCOL_ICQ -> "ICQ"
                            Im.PROTOCOL_JABBER -> "Jabber"
                            Im.PROTOCOL_NETMEETING -> "NetMeeting"
                            Im.PROTOCOL_CUSTOM -> if (cursor.getString(cursor.getColumnIndex(Im.CUSTOM_PROTOCOL)) != null) {
                                cursor.getString(cursor.getColumnIndex(Im.CUSTOM_PROTOCOL))
                            } else {
                                ""
                            }
                            else -> "other"
                        }
                        (contact?.get("instantMessengers") as ArrayList<HashMap<String, String>>).add(object : HashMap<String, String>() {
                            init {
                                put(label, username)
                            }
                        })
                    }
                }
                Organization.CONTENT_ITEM_TYPE -> {
                    contact?.put("company", cursor.getString(cursor.getColumnIndex(Organization.COMPANY)))
                    contact?.put("jobTitle", cursor.getString(cursor.getColumnIndex(Organization.TITLE)))
                    contact?.put("department", cursor.getString(cursor.getColumnIndex(Organization.DEPARTMENT)))
                }
                StructuredPostal.CONTENT_ITEM_TYPE -> (contact?.get("postalAddresses") as ArrayList<HashMap<String, String>>).add(
                    postalAddressFromCursor(
                        cursor
                    )
                )
                Event.CONTENT_ITEM_TYPE -> {
                    val eventType: Int = cursor.getInt(cursor.getColumnIndex(Event.TYPE))
                    if (eventType == Event.TYPE_BIRTHDAY) {
                        try {
                            val birthday: String =
                                cursor.getString(cursor.getColumnIndex(Event.START_DATE))
                                    .replace("--", "")
                            val yearMonthDay = birthday.split("-").toTypedArray()
                            val yearMonthDayList: List<String> = yearMonthDay.asList()
                            if (yearMonthDayList.size == 2) {
                                // birthday is formatted "12-31"
                                val month = yearMonthDayList[0].toInt()
                                val day = yearMonthDayList[1].toInt()
                                if (month in 1..12 && day >= 1 && day <= 31) {
                                    contact?.put("birthday", object : HashMap<String, Int>() {
                                        init {
                                            put("month", month)
                                            put("day", day)
                                        }
                                    })
                                }
                            } else if (yearMonthDayList.size == 3) {
                                // birthday is formatted "1986-12-31"
                                val year = yearMonthDayList[0].toInt()
                                val month = yearMonthDayList[1].toInt()
                                val day = yearMonthDayList[2].toInt()
                                if (year > 0 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                                    contact?.put("birthday", object : HashMap<String, Int>() {
                                        init {
                                            put("year", year)
                                            put("month", month)
                                            put("day", day)
                                        }
                                    })
                                }
                            }
                        } catch (e: NumberFormatException) {
                            // whoops, birthday isn't in the format we expect
                            Log.w("ContactsProvider", e.toString())
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            Log.w("ContactsProvider", e.toString())
                        } catch (e: NullPointerException) {
                            Log.w("ContactsProvider", e.toString())
                        }
                    }
                }
                Note.CONTENT_ITEM_TYPE -> contact?.put("note", cursor.getString(cursor.getColumnIndex(Note.NOTE)))
            }
        }

        promise.resolve(Arguments.makeNativeArray(map.values.toList()))
    }

    private fun postalAddressFromCursor(cursor: Cursor): HashMap<String, String> {
        val map = HashMap<String, String>()
        map["label"] = getLabel(cursor)
        putString(map, cursor, "formattedAddress", StructuredPostal.FORMATTED_ADDRESS)
        putString(map, cursor, "street", StructuredPostal.STREET)
        putString(map, cursor, "pobox", StructuredPostal.POBOX)
        putString(map, cursor, "neighborhood", StructuredPostal.NEIGHBORHOOD)
        putString(map, cursor, "city", StructuredPostal.CITY)
        putString(map, cursor, "region", StructuredPostal.REGION)
        putString(map, cursor, "state", StructuredPostal.REGION)
        putString(map, cursor, "postCode", StructuredPostal.POSTCODE)
        putString(map, cursor, "country", StructuredPostal.COUNTRY)
        return map
    }

    private fun putString(
        map: HashMap<String, String>,
        cursor: Cursor,
        key: String,
        androidKey: String
    ) {
        val value = cursor.getString(cursor.getColumnIndex(androidKey))
        if (!TextUtils.isEmpty(value)) map[key] = value
    }

    private fun getLabel(cursor: Cursor): String {
        when (cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE))) {
            StructuredPostal.TYPE_HOME -> return "home"
            StructuredPostal.TYPE_WORK -> return "work"
            StructuredPostal.TYPE_CUSTOM -> {
                val label = cursor.getString(cursor.getColumnIndex(StructuredPostal.LABEL))
                return label ?: ""
            }
        }
        return "other"
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
        const val NAME = "GalaxyCard"

        init {
            System.loadLibrary("reactnativeturboutils-jni")
        }
    }
}
