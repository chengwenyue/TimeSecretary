package justita.top.timesecretary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private TextView TimeDelayTv;
    private TextView SelectUserTv;
    private Button mBack;
    private TextView mTitleSidebarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        TimeDelayTv = (TextView) findViewById(R.id.time_delay);
        TimeDelayTv.setOnClickListener(this);
        SelectUserTv = (TextView) findViewById(R.id.user_select);
        SelectUserTv.setOnClickListener(this);
        mTitleSidebarName = (TextView) findViewById(R.id.title_sidebar_name);
        mTitleSidebarName.setText("设置");

        mBack = (Button) findViewById(R.id.back_white);
        mBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.time_delay:
                break;
            case R.id.user_select:
                startActivity(new Intent(SettingsActivity.this, UserSelectActivity.class));
                break;
            case R.id.back_white:
                finish();
                break;
        }
    }
}
