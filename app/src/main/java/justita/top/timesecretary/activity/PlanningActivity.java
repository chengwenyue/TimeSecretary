package justita.top.timesecretary.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.widget.SchedulePlanView;

public class PlanningActivity extends AppCompatActivity {
    private TextView mTitleSidebarName;
    private Button mBack;
    private SchedulePlanView mSchedulePlanView;

    private String[] timeString;
    // 例: 周一 8:00-10:40  9:00-20:00
    private Map<Integer, List<String>> timeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);
        initView();
    }

    private void initView() {
        mTitleSidebarName = (TextView) findViewById(R.id.title_sidebar_name);
        mTitleSidebarName.setText("计划");
        mSchedulePlanView = (SchedulePlanView) findViewById(R.id.plan_view);

        mBack = (Button) findViewById(R.id.back_white);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        timeMap = new HashMap<>();

        List<Affair> affairList = Affair.getAffairs(getContentResolver(), Affair.STATE + "<> ?", Affair.AFFAIR_DELETE_STATE + "");
        for (String date : get7date()) {
            // 不能创建私有类成员变量, 通过clear来清空List已经保存的数据
            // 否则原有的map中的原保存的数据也会被清空
            List<String> timeList = new ArrayList<>();

            for (Affair affair : affairList) {
                String name = affair.mTime;
                timeString = name.split(" ");
                if (date.equals(timeString[0])) {
                    if (affair.mState == Affair.AFFAIR_COMPLETE_STATE) {
                        continue;
                    }
                    if (affair.isTimeAffair()) {
                        timeList.add(timeString[1]);
                    }
                }
            }
            timeMap.put(getWeek(date), timeList);
        }

        mSchedulePlanView.setValue(timeMap);
    }

    public static List<String> get7date() {
        List<String> dates = new ArrayList<String>();
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        String date = sim.format(c.getTime());
        dates.add(date);
        for (int i = 0; i < 6; i++) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            date = sim.format(c.getTime());
            dates.add(date);
        }
        return dates;
    }

    public static int getWeek(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            return 0;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            return 1;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            return 2;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            return 3;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            return 4;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            return 5;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            return 6;
        }
        return 7;
    }
}
