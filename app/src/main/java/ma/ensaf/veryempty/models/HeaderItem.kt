package ma.ensaf.veryempty.models

class HeaderItem : UsersListItem() {

    var date: String? = null

    override val type: Int
        get() = TYPE_HEADER
}
