package justita.top.timesecretary.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.biz.OnLoginListener;
import justita.top.timesecretary.biz.UserBiz;
import justita.top.timesecretary.entity.User;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.service.DataSyncService;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;


public class LoginActivity extends BaseActivity {

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = LoginActivity.class.toString();

    private UserBiz mUserBiz = null;
    private int syncStep = 1;

    // UI references.
    private AutoCompleteTextView mNameView;
    private EditText mPasswordView;
    private Button mBack;
    private Handler mHandler = new Handler();

    private ProgressDialog mProgressDialog = null;
    private OnLoginListener mOnLoginListener = new OnLoginListener() {
        @Override
        public void loginCallback(int connectedState, String reason) {
            switch (connectedState){
                case UserBiz.CONNECTED:{
                    syncData(syncStep);
                }
                break;
                case UserBiz.CONNECTING:{
                    showProgress(true);
                }
                break;
                case UserBiz.DISCONNECTED:{
                    showProgress(false);
                    syncStep = 1;
                    Toast.makeText(LoginActivity.this, reason, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    };

    private DataSyncService.OnSyncByUserListener onSyncByUserListener = new DataSyncService.OnSyncByUserListener() {
        @Override
        public void syncFinish(int result) {
            if(result == DataSyncService.SUCCESS) {
                syncStep++;
                if(syncStep > 3) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();
                        }
                    });
                }
                syncData(syncStep);
            }else{
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private void syncData(int i) {
        switch (i){
            case 1:{
                DataSyncService.getInstance(this).syncDataByUser(this,Category.class,onSyncByUserListener);
            }case 2:{
                DataSyncService.getInstance(this).syncDataByUser(this,Affair.class,onSyncByUserListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mBack = (Button) findViewById(R.id.back);

        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                intent.putExtra("judge", "false");
                startActivity(intent);
            }
        });
        // Set up the login form.
        mNameView = (AutoCompleteTextView) findViewById(R.id.name);

        mPasswordView = (EditText) findViewById(R.id.password);
        populateAutoComplete();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
            intent.putExtra("judge", "false");
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void populateAutoComplete() {
        mNameView.setText(PreferenceUtils.getPrefString(LoginActivity.this, PreferenceConstants.ACCOUNT, ""));
        mPasswordView.setText(PreferenceUtils.getPrefString(LoginActivity.this, PreferenceConstants.PASSWORD, ""));
        mNameView.requestFocus();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void login() {
        if (mUserBiz != null) {
            return;
        }

        mNameView.setError(null);
        mPasswordView.setError(null);
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查密码
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // 检查用户名有效性
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            User user = new User();
            user.setUserName(name);
            user.setUserPwd(password);
            if (mUserBiz == null)
                mUserBiz = new UserBiz();
            Log.i(TAG, Thread.currentThread().getId() + "");
            mUserBiz.login(user, mOnLoginListener);
        }
    }

    private boolean isNameValid(String name) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (show) {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(this, null, "正在登陆..", false);
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        User user = new User();
                        mUserBiz.cancelLogin(user, mOnLoginListener);
                        mUserBiz = null;
                    }
                });
            } else if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog == null) {
                return;
            } else if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

