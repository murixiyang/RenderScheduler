// https://code.google.com/archive/p/json-simple/
// Maven: com.googlecode.json-simple:json-simple:1.1.1

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Schedule {

    private final String scheduleFile;
    private final String overridesFile;
    private Date fromTime;
    private final Date untilTime;

    private final RegularSchedule regularSchedule = new RegularSchedule();
    private final ArrayList<SingleSchedule> overrideSchedule = new ArrayList<>();

    public Schedule(String scheduleFile, String overridesFile, Date fromTime, Date untilTime) {
        this.scheduleFile = scheduleFile;
        this.overridesFile = overridesFile;
        this.fromTime = fromTime;
        this.untilTime = untilTime;
    }

    public void readSchedule() {
        JSONParser parser = new JSONParser();
        JSONObject schedule = new JSONObject();

        // Open file and return schedule JSONObject
        try {
            schedule = (JSONObject) parser.parse(new FileReader(scheduleFile));
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find file " + scheduleFile + ": " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading file " + scheduleFile + ": " + e.getMessage());
            System.exit(1);
        } catch (ParseException e) {
            System.out.println("Parse error:" + scheduleFile + ": " + e.getMessage());
            System.exit(1);
        }

        // Parse schedule
        try {
            JSONArray users = (JSONArray) parser.parse(schedule.get("users").toString());
            for (Object user : users) {
                regularSchedule.addUser((String) user);
            }
        } catch (ParseException e) {
            System.out.println("Parse error:" + scheduleFile + ".users: " + e.getMessage());
            System.exit(1);
        }

        Date handoverStartAt = DateFormatter.formatDate((String) schedule.get("handover_start_at"));
        regularSchedule.setHandOverStart(handoverStartAt);

        long handoverIntervalDays = (long) schedule.get("handover_interval_days");
        regularSchedule.setHandOverIntervalDays((int) handoverIntervalDays);
    }

    public void readOverrides() {
        JSONParser parser = new JSONParser();
        JSONArray overrides = new JSONArray();

        // Open file and return schedule JSONObject
        try {
            overrides = (JSONArray) parser.parse(new FileReader(overridesFile));
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find file " + overridesFile + ": " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading file " + overridesFile + ": " + e.getMessage());
            System.exit(1);
        } catch (ParseException e) {
            System.out.println("Parse error:" + overridesFile + ": " + e.getMessage());
            System.exit(1);
        }

        for (Object override : overrides) {
            JSONObject overrideObject = (JSONObject) override;

            String user = (String) overrideObject.get("user");
            Date startTime = DateFormatter.formatDate((String) overrideObject.get("start_at"));
            Date endTime = DateFormatter.formatDate((String) overrideObject.get("end_at"));

            overrideSchedule.add(new SingleSchedule(user, startTime, endTime));
        }
    }

    public ArrayList<JSONObject> createRenderSchedule() {
        ArrayList<SingleSchedule> renderedSchedule = new ArrayList<>();

        // Add regular schedule
        ArrayList<String> users = regularSchedule.getUsers();
        int userNum = users.size();
        int handIntervalDays = regularSchedule.getHandOverIntervalDays();

        // Calculate first schedule
        SingleSchedule firstSchedule = calculateFirstSchedule();

        int userIndex = users.indexOf(firstSchedule.getUser());
        Date roundStartTime = firstSchedule.getStartTime();
        Date roundEndTime = firstSchedule.getEndTime();

        // Add before untilTime
        while (roundEndTime.before(untilTime)) {
            SingleSchedule schedule = new SingleSchedule(users.get(userIndex), roundStartTime, roundEndTime);
            renderedSchedule.add(schedule);

            // update param for next round
            userIndex = (userIndex + 1) % userNum;
            roundStartTime = roundEndTime;
            roundEndTime = daysAfter(roundEndTime, handIntervalDays);
        }

        // add final round to untilTime
        SingleSchedule lastSchedule = new SingleSchedule(users.get(userIndex), roundStartTime, untilTime);
        renderedSchedule.add(lastSchedule);

        // add override
        for (SingleSchedule override: overrideSchedule) {
            splitSchedule(override, renderedSchedule);
        }

        // convert into JSONObject
        ArrayList<JSONObject> renderedScheduleObj = new ArrayList<>();
        for (SingleSchedule schedule: renderedSchedule) {
            JSONObject scheduleObj = createScheduleObj(schedule.getUser(), schedule.getStartTime(), schedule.getEndTime());
            renderedScheduleObj.add(scheduleObj);
        }

        return renderedScheduleObj;
    }

    // Return JSONObject according to the params given
    private JSONObject createScheduleObj(String user, Date startTime, Date endTime) {
        Map<String, String> singleSchedule = new HashMap<>();
        singleSchedule.put("user", user);
        singleSchedule.put("start_at", DateFormatter.dateToString(startTime));
        singleSchedule.put("end_at", DateFormatter.dateToString(endTime));

        return new JSONObject(singleSchedule);
    }


    // Calculate the Date that is <days> after <startDate>
    private Date daysAfter(Date startDate, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, days);

        return c.getTime();
    }

    private SingleSchedule calculateFirstSchedule() {
        Date handOverStartTime = regularSchedule.getHandOverStart();
        int interval = regularSchedule.getHandOverIntervalDays();

        // if want to query before handover starts, render to the handover start time
        if (fromTime.before(handOverStartTime)) {
            System.out.println(DateFormatter.dateToString(fromTime) +
                                " is before handover start, will display from " +
                                DateFormatter.dateToString(handOverStartTime));
            fromTime = handOverStartTime;
        }

        // calculate difference in days and rounds
        long daysDiff = calculateDaysDiff(fromTime, handOverStartTime);
        int roundsDiff = (int) daysDiff / interval;

        // the next user will be the one after <roundsDiff>
        int userNum = regularSchedule.getUsers().size();
        String userName = regularSchedule.getUsers().get(roundsDiff % userNum);

        // calculate endTime (roundDiff + 1 because needs to finish this round)
        Date endTime = daysAfter(handOverStartTime, interval * (roundsDiff + 1));

        return new SingleSchedule(userName, fromTime, endTime);
    }

    // Return the SingleSchedule happens at <getTime>
    // getTime should be later than handOverStartTime
    private SingleSchedule getOneSchedule(Date getTime) {
        Date handOverStartTime = regularSchedule.getHandOverStart();
        int interval = regularSchedule.getHandOverIntervalDays();

        // calculate difference in days and rounds
        long daysDiff = calculateDaysDiff(getTime, handOverStartTime);
        int roundsDiff = (int) daysDiff / interval;

        // the next user will be the one after <roundsDiff>
        int userNum = regularSchedule.getUsers().size();
        String userName = regularSchedule.getUsers().get(roundsDiff % userNum);

        // calculate startTime and endTime of this round
        Date startTime = daysAfter(handOverStartTime, interval * roundsDiff);
        Date endTime = daysAfter(startTime, interval);

        return new SingleSchedule(userName, startTime, endTime);
    }

    // Date1 should be later than Date 2
    private long calculateDaysDiff(Date date1, Date date2) {
        long milliSecDiff = date1.getTime() - date2.getTime();
        return TimeUnit.DAYS.convert(milliSecDiff, TimeUnit.MILLISECONDS);
    }

    /*
        Override will create splits, which looks like:
        <LeftSplit> <Override> <RightSplit>
        e.g. A1 B A2; A1 C B2; A C(overtook B) A B C

        Remove schedules before split, also needs to remove all overtook schedules
     */
    private void splitSchedule(SingleSchedule overrideSchedule, ArrayList<SingleSchedule> renderedSchedule) {
        String overrideUser = overrideSchedule.getUser();
        Date overrideStartTime = overrideSchedule.getStartTime();
        Date overrideEndTime = overrideSchedule.getEndTime();

        // search which schedule to override
        SingleSchedule startOverrideSchedule = getOneSchedule(overrideStartTime);
        SingleSchedule endOverrideSchedule = getOneSchedule(overrideEndTime);

        // Create left split, from original start to override start
        SingleSchedule leftSplit = new SingleSchedule(startOverrideSchedule.getUser(), startOverrideSchedule.getStartTime(), overrideStartTime);
        // Create right split, from override end to original end
        SingleSchedule rightSplit = new SingleSchedule(endOverrideSchedule.getUser(), overrideEndTime, endOverrideSchedule.getEndTime());

        // Generate return ArrayList
        ArrayList<SingleSchedule> splitSchedules = new ArrayList<>();
        splitSchedules.add(leftSplit);
        splitSchedules.add(overrideSchedule);
        splitSchedules.add(rightSplit);

        // Find start index in array
        int startIndex = 0;
        for (int i = 0; i < renderedSchedule.size(); i ++) {
            if (renderedSchedule.get(i).getStartTime() == startOverrideSchedule.getStartTime()) {
                startIndex = i;
            }
        }

        // Find end index in array
        int endIndex = 0;
        for (int i = startIndex; i < renderedSchedule.size(); i ++) {
            if (renderedSchedule.get(i).getStartTime() == endOverrideSchedule.getStartTime()) {
                endIndex = i;
            }
        }

        // Remove from startIndex to endIndex
        renderedSchedule.subList(startIndex, endIndex + 1).clear();

        // Add new splited entries to the rendered list
        renderedSchedule.addAll(startIndex, splitSchedules);
    }
}