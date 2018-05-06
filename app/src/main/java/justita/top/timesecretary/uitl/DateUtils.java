package justita.top.timesecretary.uitl;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {


//    // 12小时制
//    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
//    // 24小时制
//    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//
//    使用SimpleDateFormat 时，小时如果是小写的“hh”为12小时制，如果是大写的“HH”为24小时制；
//
//            c.get(Calendar.HOUR);   // 12小时制
//    c.get(Calendar.HOUR_OF_DAY); // 24小时制
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String DEFAULT_FORMAT_TWO = "yyyy-M-d";
    public static final String HOUR_MINUTE = "HH:mm";
    public static final String YEAR_MOUTH_DAY = "yyyy-MM-dd";
    public static final String MOUTH_DAY = "MM-dd";
    public static final String MOUTH_DAY_HOUR_MINUTE = "MM-dd HH:mm";

    private static final SimpleDateFormat defaultFt = new SimpleDateFormat(DEFAULT_FORMAT);

    private static final SimpleDateFormat hmFt = new SimpleDateFormat(HOUR_MINUTE);
    private static final SimpleDateFormat mdFt = new SimpleDateFormat(MOUTH_DAY);
    private static final SimpleDateFormat ymdFt = new SimpleDateFormat(YEAR_MOUTH_DAY);
    private static final SimpleDateFormat mdhmFt = new SimpleDateFormat(MOUTH_DAY_HOUR_MINUTE);
    public static String formatDate(Date date,String format){
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }
    public static String formatDate(Calendar date, String format){
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date.getTime());
    }

    public static Date formatDate(String time ,String format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format);

        return df.parse(time);
    }

    public static String defaultToHM(String time) throws ParseException {
        Date date = defaultFt.parse(time);
        return hmFt.format(date);
    }

    public static String defaultToMD(String time) throws ParseException {
        Date date = defaultFt.parse(time);
        return mdFt.format(date);
    }

    public static String defaultToYMD(String time) throws ParseException {
        Date date = defaultFt.parse(time);
        return ymdFt.format(date);
    }

    public static String defaultToMD_HM(String time) throws ParseException {
        Date date = defaultFt.parse(time);
        return mdhmFt.format(date);
    }
    public static String formatDigit(int month){
        String sign ="";
        switch (month){
            case Calendar.JANUARY :
                return "一";
            case Calendar.FEBRUARY :
                return "二";
            case Calendar.MARCH :
                return "三";
            case Calendar.APRIL :
                return "四";
            case Calendar.MAY :
                return "五";
            case Calendar.JUNE :
                return "六";
            case Calendar.JULY :
                return "七";
            case Calendar.AUGUST :
                return "八";
            case Calendar.SEPTEMBER :
                return "九";
            case Calendar.OCTOBER :
                return "十";
            case Calendar.NOVEMBER :
                return "十一";
            case Calendar.DECEMBER :
                return "十二";

        }
        return sign;
    }

    public static String formatWeek(int month){
        String sign ="";
        switch (month){
            case Calendar.SUNDAY :
                return "日";
            case Calendar.MONDAY :
                return "一";
            case Calendar.TUESDAY :
                return "二";
            case Calendar.WEDNESDAY :
                return "三";
            case Calendar.THURSDAY :
                return "四";
            case Calendar.FRIDAY :
                return "五";
            case Calendar.SATURDAY :
                return "六";

        }
        return sign;
    }

    public static final int[] DAY_ORDER = new int[] {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
    };

    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
        return format.format(new Date(time));
    }

    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    public static String getChatTime(long timesamp) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(timesamp);
        int temp = Integer.parseInt(sdf.format(today))
                - Integer.parseInt(sdf.format(otherDay));

        switch (temp) {
            case 0:
                result = "今天 " + getHourAndMin(timesamp);
                break;
            case 1:
                result = "昨天 " + getHourAndMin(timesamp);
                break;
            case 2:
                result = "前天 " + getHourAndMin(timesamp);
                break;

            default:
                // result = temp + "天前 ";
                result = getTime(timesamp);
                break;
        }

        return result;
    }
}
