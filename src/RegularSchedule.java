import java.util.ArrayList;
import java.util.Date;

public class RegularSchedule {
    private final ArrayList<String> users = new ArrayList<>();
    private Date handOverStart;
    private int handOverIntervalDays;

    public void addUser(String user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public void setHandOverStart(Date handOverStart) {
        this.handOverStart = handOverStart;
    }

    public void setHandOverIntervalDays(int handOverIntervalDays) {
        this.handOverIntervalDays = handOverIntervalDays;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public Date getHandOverStart() {
        return handOverStart;
    }

    public int getHandOverIntervalDays() {
        return handOverIntervalDays;
    }
}
