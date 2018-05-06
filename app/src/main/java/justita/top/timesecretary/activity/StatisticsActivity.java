package justita.top.timesecretary.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.widget.BarChartView;
import justita.top.timesecretary.widget.LineCharView;

public class StatisticsActivity extends BaseActivity {

    private TextView mTitleSidebarName;
    private Button mBack;
    private List<Integer> mList2 = new ArrayList<>();
    private LineCharView mLineCharView;
    private BarChartView mBarChartView;
    private TextView mAllCounts;
    private SharedPreferences mAffairCompleteCounter;
    private String[] weeksArray = {"周四", "周五", "周六", "周日", "周一", "周二", "周三"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        initView();
    }

    private void initView() {
        mLineCharView = (LineCharView) findViewById(R.id.line_char);
        mBarChartView = (BarChartView) findViewById(R.id.bar_chart);
        mAllCounts = (TextView) findViewById(R.id.all_counts);
        mTitleSidebarName = (TextView) findViewById(R.id.title_sidebar_name);
        mTitleSidebarName.setText("统计");
        mBack = (Button) findViewById(R.id.back_white);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        long userId = PreferenceUtils.getPrefLong(this, PreferenceConstants.USERID, 0);
        mAffairCompleteCounter = getSharedPreferences(userId+"_AffairCompleteCounter", this.MODE_PRIVATE);

        for (int i = 0; i < 7; i++) {
            mList2.add(getCounter(i));
        }
        mLineCharView.setValue(mList2);
        mBarChartView.setValue(mList2);
        mAllCounts.setText(mAffairCompleteCounter.getInt("总数", 0)+" 个已完成");

    }

    private int getCounter(int index){
        return mAffairCompleteCounter.getInt(weeksArray[index], 0);
    }

}
