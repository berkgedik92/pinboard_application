package project.pinboard.Services;

import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class DateManager {

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy   HH:mm");

    public String fromDateToString (Date date) {
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        return formatter.format(date);
    }
}
