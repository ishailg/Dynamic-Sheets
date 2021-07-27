import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String getRatingDayString(){
        //you can rate day x in the range: (day x) 20:00 - day (x+1) 20:00
        //check if its after 20:00
        int hour = LocalDateTime.now().getHour(); // 0-23
        if (hour < 20){ //we need to rate yestarday
            return DateTimeFormatter.ofPattern("dd/MM").format(LocalDateTime.now().minusDays(1));
        }
        return DateTimeFormatter.ofPattern("dd/MM").format(LocalDateTime.now());
    }

    public static LocalDateTime getRatingDay(){
        //you can rate day x in the range: (day x) 20:00 - day (x+1) 20:00
        //check if its after 20:00
        int hour = LocalDateTime.now().getHour(); // 0-23
        if (hour < 20){ //we need to rate yestarday
            return LocalDateTime.now().minusDays(1);
        }
        return LocalDateTime.now();
    }

    public static LocalDateTime getDateFromDM(int day, int month){
        return LocalDateTime.of(Bot.YEAR, month, day, 0, 0);
    }

    public static LocalDateTime getDateFromDM(int day, int month, int year){
        return LocalDateTime.of(year, month, day, 0, 0);
    }

    public static LocalDateTime getDateFromString(String dm){
        return getDateFromDM(Integer.parseInt(dm.split("/")[0]), Integer.parseInt(dm.split("/")[1]));
    }

    public static String dateToString(LocalDateTime date){
        return DateTimeFormatter.ofPattern("dd/MM").format(date);
    }

    public static String parseRatingMessage(LocalDateTime date, String col, String rating) {
        try{
            String dayRating;
            if ((SheetsApi.getDateRating(col, date) != null) && SheetsApi.UpdateSingleValue(SheetsApi.colDateToCell(col, date), rating)){
                return "changed the rating of "+ dateToString(date) + " to " + rating;
            }
            //update the day rating
            else if (SheetsApi.UpdateSingleValue(SheetsApi.colDateToCell(col, date), rating)){ //success
                return "succesfully rated the " + dateToString(date) + " to " + rating + "!";
            }
            //that means we didnt change
            return "error";

        } catch (Exception e){
            e.printStackTrace();
            return "error";
        }

    }
}
