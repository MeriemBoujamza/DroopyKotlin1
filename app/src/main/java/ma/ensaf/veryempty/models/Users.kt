package ma.ensaf.veryempty.models

import java.io.Serializable

//With this annotation we are going to hide compiler warnings
class Users : Serializable {
    var id = 0
    var name: String? = null
    var image: String? = null
    var location: String? = null
    var phoneNumber: String? = null
    var bloodGroup: String? = null
    var lastDonatedDate: String? = null

    constructor(
        id: Int,
        name: String?,
        image: String?,
        location: String?,
        phoneNumber: String?,
        bloodGroup: String?
    ) {
        this.id = id
        this.name = name
        this.image = image
        this.location = location
        this.phoneNumber = phoneNumber
        this.bloodGroup = bloodGroup
    }

    constructor(
        id: Int,
        name: String?,
        image: String?,
        location: String?,
        phoneNumber: String?,
        bloodGroup: String?,
        lastDonatedDate: String?
    ) {
        this.id = id
        this.name = name
        this.image = image
        this.location = location
        this.phoneNumber = phoneNumber
        this.bloodGroup = bloodGroup
        this.lastDonatedDate = lastDonatedDate
    }
}