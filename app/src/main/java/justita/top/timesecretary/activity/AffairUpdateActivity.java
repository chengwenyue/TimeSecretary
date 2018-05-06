package justita.top.timesecretary.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.fragment.AlarmFragment;
import justita.top.timesecretary.fragment.CategoryFragment;
import justita.top.timesecretary.fragment.PositionFragment;
import justita.top.timesecretary.fragment.RepeatFragment;
import justita.top.timesecretary.fragment.TimeFragment;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.AndroidBug5497Workaround;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class AffairUpdateActivity extends BaseActivity implements AndroidBug5497Workaround.OnKeyboardListener,
        View.OnClickListener{

    public static boolean isListenerKeyBoard = true;
    private int EXPAND_HEIGHT = 504;

    private boolean isExpand = false;
    private LinearLayout time_ll;
    private LinearLayout position_ll;
    private LinearLayout alarm_ll;
    private LinearLayout repeat_ll;
    private LinearLayout category_ll;

    private LinearLayout affairContent_ll;
    private EditText name_et;
    private Button mAffairSaveBt;
    private Button mAffairCancelBt;
    private View expandArea;
    private FragmentManager fragmentManager;
    private boolean isKeyBoard  = true;
    private Affair mAffair;

    private Affair.Builder mBuilder;

    private Handler mHandler = new Handler();

    private TimeFragment timeFragment;
    private PositionFragment positionFragment;
    private AlarmFragment alarmFragment;
    private RepeatFragment repeatFragment;
    private CategoryFragment categoryFragment;
    private Fragment blankFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affair_add);
        AndroidBug5497Workaround.assistActivity(this);
        mAffair = Affair.getAffair(getContentResolver(),Affair.getId(getIntent().getData()));
        if(mAffair == null)
            mAffair = new Affair();

        initView();
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public Affair.Builder getBuilder() {
        if(mBuilder == null){
            mBuilder = new Affair.Builder(mAffair);
        }
        return mBuilder;
    }
    private void initView() {
        time_ll = (LinearLayout) findViewById(R.id.time);
        position_ll = (LinearLayout) findViewById(R.id.position);
        alarm_ll = (LinearLayout) findViewById(R.id.alarm);
        repeat_ll = (LinearLayout) findViewById(R.id.repeat);
        category_ll = (LinearLayout) findViewById(R.id.category);

        affairContent_ll = (LinearLayout) findViewById(R.id.affair_content);

        affairContent_ll.setVisibility(View.INVISIBLE);
        time_ll.setOnClickListener(this);
        position_ll.setOnClickListener(this);
        alarm_ll.setOnClickListener(this);
        repeat_ll.setOnClickListener(this);
        category_ll.setOnClickListener(this);

        expandArea = findViewById(R.id.expand_area);
        name_et = (EditText) findViewById(R.id.name);

        name_et.setText(mAffair.mName);

        name_et.setFocusable(true);
        name_et.setFocusableInTouchMode(true);
        name_et.requestFocus();

        name_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabSelection(-1);
            }
        });
        //对于刚跳到一个新的界面就要弹出软键盘的情况上述代码可能由于界面为加载完全而无法弹出软键盘。
        // 此时应该适当的延迟弹出软键盘如998毫秒（保证界面的数据加载完成）。实例代码如下：

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager)name_et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(name_et, 0);
                affairContent_ll.setVisibility(View.VISIBLE);
            }
        },500);

        mAffairSaveBt = (Button) findViewById(R.id.affair_save_bt);
        mAffairSaveBt.setOnClickListener(this);
        mAffairCancelBt = (Button) findViewById(R.id.affair_cancel_bt);
        mAffairCancelBt.setOnClickListener(this);
    }
    private void expandDetail(){
        if(isExpand)
            return;
        LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams) expandArea.getLayoutParams();
        expandParams.height =EXPAND_HEIGHT;
        expandArea.requestLayout();
        isExpand = true;
    }
    private void collapseDetail(){
        if(!isExpand)
            return;
        LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams) expandArea.getLayoutParams();
        expandParams.height =0;
        expandArea.requestLayout();
        isExpand = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void OnKeyboard(boolean visible,int keyboardHeight) {
        if(!isListenerKeyBoard)
            return;
        LogUtils.e(visible +":"+keyboardHeight );
        if(visible){
            if(EXPAND_HEIGHT != 0)
                EXPAND_HEIGHT = keyboardHeight;
            setTabSelection(-1);
        }else if(isKeyBoard){
            collapseDetail();
        }
    }

    @Override
    public void onClick(View v) {
        Drawable drawable = getResources().getDrawable(R.drawable.cancel_button);
        int color = 0;
        switch (v.getId()) {
            case R.id.time:
                setTabSelection(0);
                color = getResources().getColor(R.color.fragment1);
                break;
            case R.id.position:
                setTabSelection(1);
                color = getResources().getColor(R.color.fragment2);
                break;
            case R.id.alarm:
                setTabSelection(2);
                color = getResources().getColor(R.color.fragment3);
                break;
            case R.id.repeat:
                setTabSelection(3);
                color = getResources().getColor(R.color.fragment4);
                break;
            case R.id.category:
                setTabSelection(4);
                color = getResources().getColor(R.color.fragment5);
                break;
            case R.id.affair_save_bt:{
                String affairName = name_et.getText().toString().trim();
                if(TextUtils.isEmpty(affairName)){
                    Toast.makeText(this,"请输入内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                getBuilder().setName(affairName).setUserId(PreferenceUtils.getPrefLong(this,PreferenceConstants.USERID,-1));
                Affair affair = getBuilder().create();
                Affair.updateAffair(getContentResolver(),affair);

                Intent intent  = Affair.createIntent(this,MainActivity.class,affair.mId);
                setResult(MainActivity.RESULT_SAVE,intent);
                finish();
                return;
            }
            case R.id.affair_cancel_bt:{
                this.finish();
                return;
            }
            default:
                break;
        }
        drawable.setTint(color);
        mAffairCancelBt.setBackground(drawable);
    }


    private void closeKeyboard(){
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private double mLat;
    private double mLon;
    private String mAddress;

    private void setTabSelection(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switch (index) {
            case 0:
                if (timeFragment == null) {
                    timeFragment = new TimeFragment();
                    transaction.add(R.id.expand_area, timeFragment);
                } else {
                    transaction.show(timeFragment);
                }
                break;
            case 1:
                if (positionFragment == null) {
                    positionFragment = new PositionFragment();
                    transaction.add(R.id.expand_area, positionFragment);
                } else {
                    transaction.show(positionFragment);
                }
                positionFragment.getPositionResult(new PositionFragment.positionResult() {
                    @Override
                    public void setLatitude(double latitude) {
                        mLat = latitude;
                    }

                    @Override
                    public void setLongitude(double longitude) {
                        mLon = longitude;
                    }

                    @Override
                    public void setAddress(String address) {
                        mAddress = address;
                        System.out.println("address"+address);
                    }
                });
                break;
            case 2:
                if (alarmFragment == null) {
                    alarmFragment = new AlarmFragment();
                    transaction.add(R.id.expand_area, alarmFragment);
                } else {
                    transaction.show(alarmFragment);
                }
                break;
            case 3:
                if (repeatFragment == null) {
                    repeatFragment = new RepeatFragment();
                    transaction.add(R.id.expand_area, repeatFragment);
                } else {
                    transaction.show(repeatFragment);
                }
                break;
            case 4:
                if (categoryFragment == null) {
                    categoryFragment = new CategoryFragment();
                    transaction.add(R.id.expand_area, categoryFragment);
                } else {
                    transaction.show(categoryFragment);
                }
                break;
            default:
                if (blankFragment == null) {
                    blankFragment = new Fragment();
                    transaction.add(R.id.expand_area, blankFragment);
                } else {
                    transaction.show(blankFragment);
                }
                break;
        }
        if(index != -1){
            isKeyBoard = false;
            closeKeyboard();
        }else {
            isKeyBoard = true;
        }
        expandDetail();
        transaction.commit();
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction
     *            用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (timeFragment != null) {
            transaction.hide(timeFragment);
        }
        if (positionFragment != null) {
            transaction.hide(positionFragment);
        }
        if (alarmFragment != null) {
            transaction.hide(alarmFragment);
        }
        if (repeatFragment != null) {
            transaction.hide(repeatFragment);
        }
        if (categoryFragment != null) {
            transaction.hide(categoryFragment);
        }
        if(blankFragment != null){
            transaction.hide(blankFragment);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(isKeyBoard) {
            collapseDetail();
            closeKeyboard();
            isKeyBoard = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

