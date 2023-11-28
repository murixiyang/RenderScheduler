import java.util.Date;

public class SingleSchedule {
    private final String user;
    private final Date startTime;
    private final Date endTime;

    public SingleSchedule(String user, Date startTime, Date endTime) {
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getUser() {
        return user;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return user + " from " + startTime + " to " + endTime;
    }

}
