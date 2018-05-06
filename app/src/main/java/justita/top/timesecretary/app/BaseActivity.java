package justita.top.timesecretary.app;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.LogUtils;

public class BaseActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        LogUtils.e(this.getClass().toString()+"   onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.e(this.getClass().toString()+"   onDestroy");
    }

    public Affair.Builder getBuilder(){
        return null;
    }
}
