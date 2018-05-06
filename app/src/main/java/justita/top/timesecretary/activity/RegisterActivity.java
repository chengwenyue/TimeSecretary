package justita.top.timesecretary.activity;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.biz.OnRegisterListener;
import justita.top.timesecretary.biz.UserBiz;
import justita.top.timesecretary.entity.User;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = RegisterActivity.class.toString();

    private EditText mNewNameView;
    private EditText mNewPasswordView;
    private EditText mEmailAddress;
    private Button mBack;

    private UserBiz mUserBiz;
    private ProgressDialog mProgressDialog = null;
    private OnRegisterListener mRegisterListener = new OnRegisterListener() {
        @Override
        public void registerCallback(int connectedState, String reason) {

        }

        @Override
        public void registerSuccess(String message, User user) {
            Log.i(TAG, Thread.currentThread().getId() + "");
            System.out.println(user.getUserName());
            showProgress(false);
            PreferenceUtils.setSettingLong(RegisterActivity.this,PreferenceConstants.USERID,user.getId());
            PreferenceUtils.setPrefString(RegisterActivity.this, PreferenceConstants.ACCOUNT, user.getUserName());
            PreferenceUtils.setPrefString(RegisterActivity.this, PreferenceConstants.PASSWORD, user.getUserPwd());
            PreferenceUtils.setPrefString(RegisterActivity.this, PreferenceConstants.EMAIL, user.getUserEmail());
            UserBiz.onSaveItemXml(RegisterActivity.this, user);
            Intent intent =new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void registerFailed(String message) {
            showProgress(false);
            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void registernNow() {
            showProgress(true);
        }

        @Override
        public void cancelRegister(){
            showProgress(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mBack = (Button) findViewById(R.id.back);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                intent.putExtra("judge", "false");
                startActivity(intent);
            }
        });
        mNewNameView = (EditText) findViewById(R.id.new_name);
        mNewPasswordView = (EditText) findViewById(R.id.new_password);
        mEmailAddress = (EditText) findViewById(R.id.email_address);

        Button mRegisterButton = (Button) findViewById(R.id.register);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
            intent.putExtra("judge", "false");
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void login() {
        if (mUserBiz != null) {
            return;
        }

        mNewNameView.setError(null);
        mNewPasswordView.setError(null);
        mEmailAddress.setError(null);
        String name = mNewNameView.getText().toString();
        String password = mNewPasswordView.getText().toString();
        String email = mEmailAddress.getText().toString();

        View focusView = null;
        boolean cancel = false;

        // 检测密码
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mNewPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mNewNameView;
            cancel = true;
        }

        // 检测账户名
        if (TextUtils.isEmpty(name)) {
            mNewNameView.setError(getString(R.string.error_field_required));
            focusView = mNewPasswordView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNewNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNewNameView;
            cancel = true;
        }

        // 检测邮箱
        if (TextUtils.isEmpty(email)) {
            mEmailAddress.setError(getString(R.string.error_empty_email));
            focusView = mEmailAddress;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailAddress.setError(getString(R.string.error_format_email));
            focusView = mEmailAddress;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            User user = new User();
            user.setUserName(name);
            user.setUserPwd(password);
            user.setUserEmail(email);
            if (mUserBiz == null)
                mUserBiz = new UserBiz();
            Log.i(TAG, Thread.currentThread().getId() + "");
            mUserBiz.register(user, mRegisterListener);
        }
    }

    private boolean isNameValid(String name) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        return email.matches(str);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (show) {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(this, null, "注册中..", false);
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        User user = new User();
                        mUserBiz.cancelRegister(user, mRegisterListener);
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
}

