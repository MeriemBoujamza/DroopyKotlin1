package ma.ensaf.veryempty.models

abstract class UsersListItem {
    abstract val type: Int

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ROW = 1
    }
}
