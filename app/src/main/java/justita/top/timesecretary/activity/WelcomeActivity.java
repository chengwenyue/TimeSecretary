package justita.top.timesecretary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public class WelcomeActivity extends BaseActivity {

    private String TAG = this.getClass().toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.initDeviceScreenSize(this);


        //如果账户登陆过则后台自动登陆，并且跳转到主界面
        String judge = null;
        Intent intent = getIntent();
        judge = intent.getStringExtra("judge");
        if(PreferenceUtils.hasKey(this, PreferenceConstants.ACCOUNT ) && judge == null){

            Intent intent1= new Intent(this, XXService.class);
            intent1.setAction(XXService.LOGIN_ACTION);
            startService(intent1);
            startActivity(new Intent(this,MainActivity.class));
            this.finish();
        }
        setContentView(R.layout.activity_welcome);
    }

    public void login(View v){
        Intent loginIntent = new Intent(this,LoginActivity.class);
        startActivity(loginIntent);
    }

    public void register(View v){
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
