package ma.ensaf.veryempty.models;

public abstract class UsersListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ROW = 1;

    abstract public int getType();
}
