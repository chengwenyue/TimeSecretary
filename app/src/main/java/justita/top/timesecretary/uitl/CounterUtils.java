package justita.top.timesecretary.uitl;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CounterUtils {

    public static void affairCompleteCounter(SharedPreferences affairCompleteCounter,
                                             SharedPreferences.Editor editor) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String week = "周";
        Calendar c = Calendar.getInstance();
        Date  curDate = new Date(System.currentTimeMillis());
        String time = format.format(curDate);
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                week += "一";
                break;
            case 2:
                week += "二";
                break;
            case 3:
                week += "三";
                break;
            case 4:
                week += "四";
                break;
            case 5:
                week += "五";
                break;
            case 6:
                week += "六";
                break;
            case 7:
                week += "日";
                break;
        }
        int counter = affairCompleteCounter.getInt(time + " " + week, 0) + 1;
        editor.putInt(time + " " + week, counter);
        editor.putInt(week, counter);
        int allCounters = affairCompleteCounter.getInt("总数", 0) + 1;
        editor.putInt("总数", allCounters);
        editor.commit();
    }
}
