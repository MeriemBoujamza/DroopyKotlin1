package ma.ensaf.veryempty.models;

public class HeaderItem extends UsersListItem {

    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}

