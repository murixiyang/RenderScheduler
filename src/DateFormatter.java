import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static Date formatDate(String dateString) {
        // Remove single quotes from the date string
        dateString = dateString.replace("'", "");

        Date result = new Date();
        try {
            result = dateFormat.parse(dateString);
        } catch (ParseException e) {
            System.out.println("Fail to parse date: " + dateString);
            System.exit(1);
        }

        return result;
    }

    public static String dateToString(Date date) {
        return dateFormat.format(date);
    }
}
