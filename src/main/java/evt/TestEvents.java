package evt;

import com.sun.jna.platform.win32.Advapi32Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestEvents {

    public static void main(String[] args) {

        // any name is valid, correct name will filter?
        String source = "System"; // working.
//        String source = "Security";  // working. requires admin user
//        String source = "Setup";  // not working? same result as "any" text
//        String source = "Application"; // "any" text might be "Application"

        Advapi32Util.EventLogIterator eli = new Advapi32Util.EventLogIterator(source);

        // get total number of records (with according level)
        int total = 0;
        while (eli.hasNext()) {
            if (checkLevel(eli.next())) {
                total++;
            }
        }
        System.out.println("got count=" + total + " for source=" + source + " (with specific level)\n");

        eli = new Advapi32Util.EventLogIterator(source);
        int count = 0;
        int last = 20;
        while (eli.hasNext()) {  // this is sorted by record ID. oldest record first.
            Advapi32Util.EventLogRecord record = eli.next();

            if (checkLevel(record) && (++count > total - last)) {
                System.out.println(formatDate(record) + " ev=" + formatNumber((record.getEventId() & 0xFFFF), 5)
                        + " nr=" + formatNumber(record.getRecordNumber(), 5)
                        + " type=" + formatType(record.getType()) + " src='" + record.getSource() + "'");
            }
        }
    }

    // used to filter logs
    private static boolean checkLevel(Advapi32Util.EventLogRecord record) {
//        return true;
        return record.getType() == Advapi32Util.EventLogType.Warning || record.getType() == Advapi32Util.EventLogType.Error;
    }

    private static String formatDate(Advapi32Util.EventLogRecord record) {
        Date date = new Date(record.getRecord().TimeWritten.longValue() * 1000);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    // format fixed length with leading zeros
    private static String formatNumber(int number, int digits) {
        return String.format("%0" + digits + "d", number);
    }

    // format type (single char E,W,I, ...)
    private static String formatType(Advapi32Util.EventLogType type) {
        return type.toString().substring(0, 1);
    }
}
