package `in`.galaxycard.android.utils

data class Contact(val id: String, val name: String?) {
    var firstName: String? = null
    var middleName: String? = null
    var lastName: String? = null
    var photo: String? = null

    var numbers = ArrayList<String>()
    var emails = ArrayList<String>()
}