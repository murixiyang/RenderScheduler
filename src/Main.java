import java.util.Date;

public class Main {

    /*
        Args: String scheduleFileName, String overridesFileName, String queryFromTime, String queryUntilTime
        Return: ArrayList<JSONObject> renderedSchedule
     */
    public static void main(String[] args) {
        String scheduleFile = null;
        String overridesFile = null;
        String fromTime = null;
        String untilTime = null;

        // Processing arguments
        for (String arg : args) {
            String[] argParts = arg.split("=");
            switch (argParts[0]) {
                case "--schedule" -> scheduleFile = argParts[1];
                case "--overrides" -> overridesFile = argParts[1];
                case "--from" -> fromTime = argParts[1];
                case "--until" -> untilTime = argParts[1];
                default -> {
                    System.out.println("Unknown argument: " + arg);
                    System.exit(1);
                }
            }
        }

        // If not enough arguments
        if (scheduleFile == null || overridesFile == null || fromTime == null || untilTime == null) {
            System.out.println("Missing arguments.");
            System.out.println("Usage: ./render-schedule --schedule=<fileName.json> " +
                                                           "--overrides=<fileName.json> " +
                                                           "--from=<yyyy-MM-dd'T'HH:mm:ss'Z'> " +
                                                           "--until=<yyyy-MM-dd'T'HH:mm:ss'Z'>");
            System.exit(1);
        }

        // Format date
        Date fromTimeDate = DateFormatter.formatDate(fromTime);
        Date untilTmeDate = DateFormatter.formatDate(untilTime);


        // Create schedule
        Schedule schedule = new Schedule(scheduleFile, overridesFile, fromTimeDate, untilTmeDate);

        schedule.readSchedule();
        schedule.readOverrides();
        var renderedSchedule = schedule.createRenderSchedule();
        System.out.println(renderedSchedule);
    }
}
