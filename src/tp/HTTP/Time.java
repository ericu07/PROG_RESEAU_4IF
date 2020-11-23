import java.util.Calendar;
public class Time {
    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        System.out.println(c.get(Calendar.DAY_OF_WEEK) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR) + ", " + c.get(Calendar.HOUR) + "h" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));
    }
}