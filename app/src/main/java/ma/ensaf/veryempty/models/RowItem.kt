package ma.ensaf.veryempty.models



class RowItem : UsersListItem() {

    var users: Users? = null

    override val type: Int
        get() = TYPE_ROW
}
